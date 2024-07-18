package org.kookies.mirai.commen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kookies.mirai.commen.adapter.LocalDateAdapter;
import org.kookies.mirai.commen.constant.GaodeAPIConstant;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.constant.WordCloudConstant;
import org.kookies.mirai.commen.exceptions.DataLoadException;
import org.kookies.mirai.commen.info.DataPathInfo;
import org.kookies.mirai.commen.utils.FileManager;
import org.kookies.mirai.pojo.dto.PoiDTO;


import java.awt.*;
import java.awt.Font;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.time.format.DateTimeFormatter;


/**
 * @author General_K1ng
 */
public class ConfigurationLoader {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private static final File CONFIG = new File(DataPathInfo.CONFIG_PATH);

    private static final File EAT_POI = new File(DataPathInfo.EAT_WHAT_POI_PATH);

    private static final File FONTS_DIR = new File(DataPathInfo.FONTS_PATH);

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * 对外开放的方法
     * 用于加载配置文件
     */
    public static void init() {
        // 如果配置文件不存在，则按照 resources/template下的 configurationTemplate.json 创建一个配置文件
        try {
            if (!CONFIG.exists()){
                CONFIG.getParentFile().mkdirs();
                String templateFile = FileManager.readTemplateFile(DataPathInfo.CONFIG_TEMPLATE_PATH);
                FileManager.write(CONFIG.getPath(), templateFile);
            } else {
                update();
            }

            initEatPoi();
            installFonts();
            // 读取配置文件并解析为 Config 对象
//            JsonObject jsonObject = FileManager.readJsonFile(CONFIG.getPath());
//            Config CONFIG = GSON.fromJson(jsonObject, Config.class);
//            ConfigContext.setConfig(CONFIG);  // 设置到 ThreadLocal 中
        } catch (IOException e) {
            throw new DataLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
    }

    /**
     * 用于更新配置文件
     */
    private static void update() {
        try {
            JsonObject jsonObject = FileManager.readJsonFile(CONFIG.getPath());
            FileManager.writeJsonFile(CONFIG.getPath(), jsonObject);
        } catch (IOException e) {
            throw new DataLoadException(MsgConstant.CONFIG_UPDATE_ERROR);
        }
    }

    /**
     * 初始化吃喝点位信息。
     * <p>
     * 该方法首先检查吃喝点位信息文件是否存在，如果不存在，则创建文件并写入从外部获取的点位信息；
     * 如果文件存在，则直接读取并覆盖该文件内容。
     * 该过程如果出现IO异常，则抛出配置加载异常。
     */
    private static void initEatPoi() {
        try {
            // 检查吃喝点位信息文件是否存在，不存在则创建其父目录并初始化文件
            if (!EAT_POI.exists()) {
                // 创建父目录
                EAT_POI.getParentFile().mkdirs();
                // 获取吃喝点位信息
                List<PoiDTO> poiList = getEatPoi();
                // 将点位信息转换为Json数组
                JsonArray jsonArray = GSON.toJsonTree(poiList).getAsJsonArray();
                // 写入Json数组到文件
                FileManager.write(EAT_POI.getPath(), jsonArray.toString());
            } else {
                // 文件存在，直接读取并覆盖文件内容
                JsonArray jsonArray = FileManager.readJsonArray(EAT_POI.getPath());
                // 再次写入Json数组到同一文件，实现覆盖
                FileManager.write(EAT_POI.getPath(), jsonArray.toString());
            }
        } catch (IOException e) {
            // 处理IO异常，抛出配置加载异常
            throw new DataLoadException(MsgConstant.CONFIG_LOAD_ERROR);
        }
    }


    /**
     * 获取指定类别（美食）的POI（Point of Interest）数据。
     * <p>
     * 该方法从预先配置的Excel文件中读取数据，筛选出符合指定大类别的POI信息，并返回这些信息的列表。
     *
     * @return List<PoiDTO> 返回一个包含符合条件的POI数据传输对象的列表。
     */
    private static List<PoiDTO> getEatPoi() {
        // 目标类别，即要筛选的POI的大类别
        String targetCategory = GaodeAPIConstant.TARGET_CATEGORY;

        List<PoiDTO> poiList = new ArrayList<>();
        // 尝试从资源文件中读取Excel数据，并使用BufferedInputStream提高读取效率
        try (InputStream is = Objects.requireNonNull(ConfigurationLoader.class.getResourceAsStream(DataPathInfo.MAP_POI_PATH));
             BufferedInputStream bis = new BufferedInputStream(is);
             Workbook workbook = new XSSFWorkbook(bis)) {

            // 获取Excel工作表的第0个Sheet
            Sheet sheet = workbook.getSheetAt(0);

            // 遍历Sheet中的每一行，忽略第一行（标题行）
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                // 获取并处理当前行的第2列（大类别）的单元格类型
                CellType cellType = row.getCell(1).getCellType();

                // 根据当前行的数据构建PoiDTO对象
                PoiDTO poiDTO = PoiDTO.builder()
                        .id(Long.parseLong(getCellValue(row.getCell(0))))
                        .newType(getCellValue(row.getCell(1)))
                        .bigCategory(getCellValue(row.getCell(2)))
                        .midCategory(getCellValue(row.getCell(3)))
                        .subCategory(getCellValue(row.getCell(4)))
                        .build();

                // 如果当前POI的大类别与目标类别匹配，则将其添加到结果列表中
                if (targetCategory.equals(poiDTO.getBigCategory())) {
                    poiList.add(poiDTO);
                }
            }

        // 捕获处理Excel文件读取过程中可能发生的IOException
        } catch (IOException e) {
            // 抛出配置加载异常，封装原始的IOException
            throw new DataLoadException(MsgConstant.EXCEL_LOAD_ERROR);
        }

        return poiList;
    }

    /**
     * 获取单元格的值。
     * <p>
     * 此方法根据单元格的类型（数值、字符串、布尔值、公式、空单元格或错误值）来返回相应的值。
     * 对于数值单元格，如果它被格式化为日期，则以指定的日期格式返回日期值；
     * 否则，以字符串形式返回数值。
     *
     * @param cell 表示要获取值的单元格。
     * @return 返回单元格的值，根据单元格类型不同，返回值的类型也不同，可能是字符串、日期、布尔值等。
     */
    private static String getCellValue(Cell cell) {
        String cellValue;
        // 根据单元格的类型来处理并返回相应的值
        switch (cell.getCellType()) {
            case NUMERIC:
                // 如果单元格被格式化为日期，则以字符串形式返回日期值
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                    return DATE_FORMATTER.format(date);
                } else {
                    // 如果不是日期格式的数值，以字符串形式返回
                    return DATA_FORMATTER.formatCellValue(cell);
                }
            case STRING:
                // 对于字符串类型的单元格，直接返回字符串值
                return cell.getStringCellValue();
            case BOOLEAN:
                // 对于布尔类型的单元格，以字符串形式返回布尔值
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // 如果单元格包含公式，返回该公式
                return cell.getCellFormula();
            case BLANK:
                // 对于空单元格，返回空字符串
                return "";
            case ERROR:
                // 对于包含错误的单元格，返回"ERROR"
                return "ERROR";
            default:
                // 对于未识别的单元格类型，返回"N/A"
                return "N/A";
        }
    }

    /**
     * 安装所需的字体。
     * <p>
     * 检查当前系统中是否缺少指定的字体，如果缺少，则从指定目录复制这些字体文件到系统字体目录。
     * 这个方法主要用于确保系统中具有用于生成词云图所需的全部字体。
     */
    private static void installFonts() {
        // 获取本地图形环境，用于检查系统中已安装的字体
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // 获取系统中所有可用的字体名称
        String[] fontNames = ge.getAvailableFontFamilyNames();
        // 定义需要检查的字体列表
        String[] needFonts = WordCloudConstant.FONTS;

        // 遍历需要检查的字体列表
        for (String needFont : needFonts) {
            // 检查系统中是否已存在指定的字体
            if (!Arrays.asList(fontNames).contains(needFont)) {
                try {
                    // 获取字体文件所在的目录
                    File[] fontFiles = FONTS_DIR.listFiles();

                    for (File fontFile : Objects.requireNonNull(fontFiles)) {
                        // 如果文件名包含需要安装的字体名称，则注册字体
                        if (fontFile.getName().contains(needFont)) {
                            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                        }
                    }
                } catch (IOException | FontFormatException e) {
                    // 如果在处理字体文件过程中发生IO异常，则抛出数据加载异常
                    throw new DataLoadException(MsgConstant.FONT_INSTALL_ERROR);
                }
            }
        }
    }


}
