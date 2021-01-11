package demo.sql.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;
import rx.functions.Action2;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;


/**
 * 写入excel
 */
@Slf4j
public class ExcelUtil {
    private Workbook workbook;
    private int initSheetIndex = 0; //页码
    private int titleRowNum = 0; //标题行

    private Map<String, Integer> map;//解析title

    public enum Type {
        XLS("xls"), XLSX("xlsx");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public static Type forType(String value) {
            for (Type type : Type.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }


    /**
     * 根据输入流和格式构造
     *
     * @param in
     * @param type
     */
    public ExcelUtil(InputStream in, Type type) {
        Assert.notNull(type, "Type can not be null");
        try {
            if (type == Type.XLS) {
                workbook = new HSSFWorkbook(in);
            } else if (type == Type.XLSX) {
                workbook = new XSSFWorkbook(in);
            }
        } catch (IOException e) {
            log.error("Read spreadsheet error!", e);
        }
    }

    public ExcelUtil(InputStream in, Type type, int initSheetIndex, int titleRowNum) {
        Assert.notNull(type, "Type can not be null");
        try {
            if (type == Type.XLS) {
                workbook = new HSSFWorkbook(in);
            } else if (type == Type.XLSX) {
                workbook = new XSSFWorkbook(in);
            }
            this.initSheetIndex = initSheetIndex;
            this.titleRowNum = titleRowNum;
        } catch (IOException e) {
            log.error("Read spreadsheet error!", e);
        }
    }

    /**
     * 用于构建 Workbook -> 这个要根据实际来，不然会报错
     *
     * @param in
     * @param type
     * @return
     * @throws IOException
     */
    public static <T> T buildWorkbook(InputStream in, Type type) throws IOException {
        Assert.notNull(type, "Type can not be null");
        if (type == Type.XLS) {
            return (T) new HSSFWorkbook(in);
        } else if (type == Type.XLSX) {
            return (T) new XSSFWorkbook(in);
        }
        return null;
    }

    /**
     * 根据Workbook构造（一个xml）
     *
     * @param workbook
     */
    public ExcelUtil(Workbook workbook) {
        Assert.notNull(workbook, "Workbook can not be null");
        this.workbook = workbook;
    }

    public ExcelUtil(Workbook workbook, int initSheetIndex, int titleRowNum) {
        Assert.notNull(workbook, "Workbook can not be null");
        this.workbook = workbook;
        this.initSheetIndex = initSheetIndex;
        this.titleRowNum = titleRowNum;
    }


    /**
     * vo 直接写入excel
     *
     * @param vos
     */
    public <T> void writeVosAAppend(List<T> vos, int sheetIndex) {
        int lastRowNum = this.workbook
                .getSheetAt(sheetIndex).getLastRowNum();
        for (T vo : vos) {
            try {
                Row row = this.workbook
                        .getSheetAt(sheetIndex).createRow(++lastRowNum);
                log.info("num:{}", lastRowNum);
                int i = 0;
                for (Field field : vo.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Cell cell = row.createCell(i++);
                    cell.setCellFormula("string");
                    String value = null == field.get(vo) ? "" : field.get(vo).toString();
                    cell.setCellValue(value);
                }
            } catch (Exception e) {
                log.info("写入异常:{}", e.toString(), e);
            }
        }
    }

    /**
     * 输出到流
     */
    public void write(OutputStream outputStream) throws IOException {
        this.workbook.write(outputStream);
    }

    /**
     * 创建XLS格式,并写入文件
     *
     * @param vos
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> HSSFWorkbook writeVosAppendXLS(List<T> vos, OutputStream outputStream) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        int line = 0;
        for (T vo : vos) {
            try {
                HSSFRow row = sheet.createRow(line++);//Sheet1
                int low = 0;
                for (Field field : vo.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Cell cell = row.createCell(low++);
                    cell.setCellType(CellType.STRING);
                    String value = null == field.get(vo) ? "" : field.get(vo).toString();
                    cell.setCellValue(value);
                }
            } catch (Exception e) {
                log.info("写入异常:{}", e.toString(), e);
            }
        }
        workbook.write(outputStream);
        return workbook;
    }

    /**
     * @param vos
     * @param outputStream
     * @param process      line 处理的vos
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> XSSFWorkbook writeVosAppendXLSX(List<T> vos, OutputStream outputStream, Action2<Integer, Integer> process) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet");
        int line = 0;
        int size = vos.size();
        boolean title = false;
        for (T vo : vos) {
            XSSFRow row = sheet.createRow(line++);//Sheet1
            int low = 0;
            for (Field field : vo.getClass().getDeclaredFields()) {
                if (title == false) {
                    field.setAccessible(true);
                    Cell cell = row.createCell(low++);
                    cell.setCellType(CellType.STRING);
                    String value = field.getName();
                    cell.setCellValue(value);
                    title = true;
                }
                field.setAccessible(true);
                Cell cell = row.createCell(low++);
                cell.setCellType(CellType.STRING);
                String value = null == field.get(vo) ? "" : field.get(vo).toString();
                cell.setCellValue(value);
            }
            if (null != process) {
                process.call(line, size);//留作处理进度
            }
            if (line % 50000 == 0) {
                workbook.write(outputStream);
                sheet = workbook.createSheet("sheet" + line % 50000);
            }
        }

        return workbook;
    }


    /**
     * 插入
     *
     * @throws IOException
     */
    public static <T> SXSSFWorkbook writeVosSXSS(List<T> vos, OutputStream outputStream, boolean title, String sheetName, Action2<Integer, Integer> process)
            throws IllegalAccessException, IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        int line = 0;
        int size = vos.size();
        if (title == true) {
            /**
             * 处理title
             */
            int low = 0;
            SXSSFRow row = sheet.createRow(line++);
            for (Field field : vos.get(0).getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Cell cell = row.createCell(low++);
                cell.setCellType(CellType.STRING);
                String value = field.getName();
                cell.setCellValue(value);
            }
        }

        for (T vo : vos) {
            SXSSFRow row = sheet.createRow(line++);//sheet1
            int low = 0;
            for (Field field : vo.getClass().getDeclaredFields()) {
                if (line == 1) {

                }
                field.setAccessible(true);
                Cell cell = row.createCell(low++);
                cell.setCellType(CellType.STRING);
                String value = null == field.get(vo) ? "" : field.get(vo).toString();
                cell.setCellValue(value);
            }
            if (null != process) {
                process.call(line, size);//留作处理进度
            }
        }
        workbook.write(outputStream);
        return workbook;
    }


    /**
     * 插入
     *
     * @throws IOException
     */
    public static <T> SXSSFWorkbook writeVosSXSS(List<T> vos, OutputStream outputStream, boolean title, String sheetName)
            throws IllegalAccessException, IOException {
        return writeVosSXSS(vos, outputStream, title, sheetName, null);
    }


    /**
     * 插入
     *
     * @throws IOException
     */
    public static <T> SXSSFWorkbook writeVosSXSS(List<T> vos, OutputStream outputStream, boolean title, Action2<Integer, Integer> process)
            throws IllegalAccessException, IOException {
        return writeVosSXSS(vos, outputStream, title, "Sheet1", process);
    }

    /**
     * 插入
     *
     * @throws IOException
     */
    public static <T> SXSSFWorkbook writeVosSXSS(List<T> vos, OutputStream outputStream, boolean title)
            throws IllegalAccessException, IOException {
        return writeVosSXSS(vos, outputStream, title, "Sheet1", null);
    }

    /**
     * 插入
     *
     * @throws IOException
     */
    public static SXSSFWorkbook writeListSXSS(List<List<String>> data, OutputStream outputStream, String sheetName, Action2<Integer, Integer> process)
            throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        int line = 0;
        int size = data.size();

        for (List<String> list : data) {
            SXSSFRow row = sheet.createRow(line++);//sheet1
            int low = 0;
            for (String item : list) {
                Cell cell = row.createCell(low++);
                cell.setCellType(CellType.STRING);
                String value = item;
                cell.setCellValue(value);
            }
            if (null != process) {
                process.call(line, size);//留作处理进度
            }
        }
        workbook.write(outputStream);
        return workbook;
    }

    /**
     * 插入
     *
     * @throws IOException
     */
    public static XSSFWorkbook writeListXLS(List<List<String>> data, OutputStream outputStream, String sheetName, Action2<Integer, Integer> process)
            throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);
        int line = 0;
        int size = data.size();

        for (List<String> list : data) {
            XSSFRow row = sheet.createRow(line++);//sheet1
            int low = 0;
            for (String item : list) {
                Cell cell = row.createCell(low++);
                cell.setCellType(CellType.STRING);
                String value = item;
                cell.setCellValue(value);
            }
            if (null != process) {
                process.call(line, size);//留作处理进度
            }
        }
        workbook.write(outputStream);
        return workbook;
    }

    /**
     * 插入
     *
     * @throws IOException
     */
    public static SXSSFWorkbook writeListSXSS(List<List<String>> data, OutputStream outputStream, Action2<Integer, Integer> process)
            throws IllegalAccessException, IOException {
        return writeListSXSS(data, outputStream, "Sheet1", process);
    }


    /**
     * 插入
     *
     * @throws IOException
     */
    public static XSSFWorkbook writeListXLS(List<List<String>> data, OutputStream outputStream, Action2<Integer, Integer> process)
            throws IllegalAccessException, IOException {
        return writeListXLS(data, outputStream, "Sheet1", process);
    }

    /**
     * 插入
     *
     * @throws IOException
     */
    public static XSSFWorkbook writeListXLS(List<List<String>> data, OutputStream outputStream)
            throws IllegalAccessException, IOException {
        return writeListXLS(data, outputStream, "Sheet1", null);
    }


    /**
     * 插入
     *
     * @throws IOException
     */
    public static SXSSFWorkbook writeListSXSS(List<List<String>> data, OutputStream outputStream)
            throws IllegalAccessException, IOException {
        return writeListSXSS(data, outputStream, "Sheet1", null);
    }

    /**
     * 插入
     *
     * @throws IOException
     */
    public static SXSSFWorkbook writeListSXSS(List<List<String>> data, File file)
            throws IllegalAccessException, IOException {
        OutputStream outputStream = new FileOutputStream(file);
        SXSSFWorkbook sxssfWorkbook = writeListSXSS(data, outputStream, "Sheet1", null);
        outputStream.close();
        return sxssfWorkbook;
    }

    /**
     * 从SXSS读取
     *
     * @throws IOException
     */
    public static List<List<String>> readListSXSS(XSSFWorkbook workbook, String sheetName, Action2<Integer, Integer> process) {
        List<List<String>> data = new ArrayList<>();
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
        SXSSFSheet sheet = sxssfWorkbook.getSheet(sheetName);

        int lastRowNum = sheet.getLastRowNum();
        for (int line = 0; line <= lastRowNum; line++) {
            List<String> list = new ArrayList<>();
            SXSSFRow row = sheet.getRow(line);
            short lastCellNum = row.getLastCellNum();
            for (int rowNum = 0; rowNum <= lastCellNum; rowNum++) {
                SXSSFCell cell = row.getCell(rowNum);
                list.add(cell.getStringCellValue());
            }
            data.add(list);
            if (null != process) {
                process.call(line, lastRowNum);
            }
        }
        return data;
    }

    /**
     * 读取execl,未解决大数据问题
     *
     * @throws IOException
     */
    public static List<List<String>> readList(Workbook workbook, String sheetName, Action2<Integer, Integer> process) {
        List<List<String>> data = new ArrayList<>();
        Sheet sheet = workbook.getSheet(sheetName);
        int lastRowNum = sheet.getLastRowNum();
        for (int line = 0; line <= lastRowNum; line++) {
            List<String> list = new ArrayList<>();
            Row row = sheet.getRow(line);
            if (null != row) {
                Short lastCellNum = row.getLastCellNum();
                for (int rowNum = 0; rowNum <= lastCellNum; rowNum++) {
                    Cell cell = row.getCell(rowNum);
                    String value = cell == null ? "" : cell.getStringCellValue();
                    list.add(value);
                }
                data.add(list);
                if (null != process) {
                    process.call(line, lastRowNum);
                }
            }
        }
        return data;
    }


    /**
     * 从SXSS读取
     *
     * @throws IOException
     */
    public static List<List<String>> readListSXSS(XSSFWorkbook workbook) {
        return readListSXSS(workbook, "Sheet1", null);
    }


    /**
     * 从SXSS读取 -> 这里目前只处理 XLSX
     *
     * @throws IOException
     */
    public static List<List<String>> readList(InputStream inputStream, Type type, Action2<Integer, Integer> process) throws IOException {
        XSSFWorkbook workbook = buildWorkbook(inputStream, type);
        return readList(workbook, "Sheet1", process);
    }

    /**
     * 从SXSS读取 -> 这里目前只处理 XLSX
     *
     * @throws IOException
     */
    public static List<List<String>> readList(InputStream inputStream, Type type) throws IOException {
        XSSFWorkbook workbook = buildWorkbook(inputStream, type);
        return readList(workbook, "Sheet1", null);
    }

    public static <T> void writeVosSXSSLog(String filePath, List<T> list) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        try {
            ExcelUtil.writeVosSXSS(list, outputStream, true, new Action2<Integer, Integer>() {
                @Override
                public void call(Integer line, Integer size) {
                    log.info("处理进度:{}/{}->{}", line, size, percent(line, size));
                }
            });
            outputStream.close();
        } catch (Exception e) {
            log.info("写入异常:{}", e.toString(), e);
        }

    }


    /**
     * 获取百分比
     *
     * @param p1
     * @param p2
     * @return
     */
    public static String percent(double p1, double p2) {
        String str;
        double p3 = p1 / p2;
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        str = nf.format(p3);
        return str;
    }

    /**
     * 写入CSV
     */
    public static void writeListCSV(Collection<List<String>> collection,
                                    OutputStream outputStream,
                                    Action2<Integer, Integer> process) throws IOException {
        //初始化csvformat
        CSVFormat formator = CSVFormat.DEFAULT;
        //创建FileWriter对象
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        //创建CSVPrinter对象
        CSVPrinter printer = new CSVPrinter(outputStreamWriter, formator);
        if (null != collection) {
            int line = 0;
            int size = collection.size();
            for (List<String> lineData : collection) {
                if (null != process) {
                    process.call(line, size);//留作处理进度
                }
                line++;
                printer.printRecord(lineData);
            }
        }
        printer.flush();//必须要添加,不然会在缓存中
    }

    /**
     * 读出CSV
     */
    public static Collection<List<String>> ReadListCSV(InputStream inputStream,
                                                       Action2<Integer, Integer> process) throws IOException {
        Collection<List<String>> result = new ArrayList<>();
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);// 定义后必须和csv文件中的标头一致
        int line = 0;
        for (CSVRecord csvRecord : records) {
            line++;
            Iterator<String> iterator = csvRecord.iterator();
            List<String> tmpList = IteratorUtils.toList(iterator);
            result.add(tmpList);
        }
        reader.close();
        return result;
    }

    /**
     * 读出CSV
     */
    public static Collection<List<String>> ReadListCSV(File file,
                                                       Action2<Integer, Integer> process) throws IOException {
        return ReadListCSV(new FileInputStream(file), process);
    }


}
