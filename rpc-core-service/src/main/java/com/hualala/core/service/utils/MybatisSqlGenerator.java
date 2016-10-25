package com.hualala.core.service.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Created by xiangbin on 2016/8/26.
 */
public class MybatisSqlGenerator {

    private String dbUrl = "jdbc:mysql://mu.mysql.001.master.hualala.com:3306/information_schema";
    private String dbUser = "root";
    private String dbPassword = "gozapdev";
    private String packagePrefix = "com.hualala.app.";

    public MybatisSqlGenerator() {

    }

    public MybatisSqlGenerator(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl + "/information_schema";
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public void generatorMySql(String database, String tableName) {
        generatorMySql(database, new String[] {tableName});
    }

    public void generatorMySql(String database, String tableName, String outputParam) {
        String packageName = database.substring(database.indexOf("_") + 1);
        String tablePrefix = "tbl_" + packageName + "_";
        generatorMySql(database, tableName, tablePrefix, packagePrefix + packageName, outputParam);
    }
    /**
     * 生成SQL
     * @param database 数据库名
     * @param tableNames 表名
     */
    public void generatorMySql(String database, String[] tableNames) {
        String packageName = database.substring(database.indexOf("_") + 1);
        String tablePrefix = "tbl_" + packageName + "_";
        generatorMySql(database, tableNames, packagePrefix + packageName, tablePrefix);
    }



    public void generatorMySql(String database, String[] tableNames, String packageName, String tablePrefix) {
        for (String tableName : tableNames) {
            generatorMySql(database, tableName, tablePrefix, packageName, "src/main");
        }
    }

    public void generatorMySql(String database, String tableName, String tablePrefix, String packageName,  String outputParam) {
        try {
            List<Map<String, String>> fieldList = queryFieldList(database, tableName);
            if (fieldList.size() == 0) {
                System.out.println("field size 000..............");
                return;
            }
            String outputF = "src/main";
            if (outputParam != null) {
                outputF = outputParam;
            }
            VelocityEngine ve = getVelocityEngine();
            VelocityContext context = new VelocityContext();
            context.put("fieldLst", fieldList);
            context.put("tableName", tableName);
            String className = "";
            String mapperNameStr = tableName.startsWith(tablePrefix) ? tableName.substring(tablePrefix.length()) : tableName;
            int startIndex = 0;
            int index = mapperNameStr.indexOf("_", startIndex);
            while (index > -1) {
                className = className + Character.toUpperCase(mapperNameStr.charAt(startIndex)) + mapperNameStr.substring(startIndex + 1, index);
                startIndex = index + 1;
                index = mapperNameStr.indexOf("_", startIndex);
            }
            className = className + Character.toUpperCase(mapperNameStr.charAt(startIndex)) + mapperNameStr.substring(startIndex + 1);
            String mapperName = packageName + "." + "mapper." + className + "Mapper";
            String modelName = packageName + "." + "model." + className + "Model";
            context.put("mapperName", mapperName);
            context.put("mapperNameField", Character.toLowerCase(mapperName.charAt(0)) + mapperName.substring(1));
            context.put("modelName", modelName);
            context.put("modelClassName", className + "Model");
            context.put("mapperClassName", className + "Mapper");
            context.put("packageMapperName", packageName + "." + "mapper");
            context.put("packageModelName", packageName + "." + "model");
            context.put("modelImportName", packageName + "." + "model");

            Template template = ve.getTemplate("/template/template-mysql");
            String pathName = packageName.replaceAll("\\.", "/");
            File output = new File(outputF + "/resources/" + pathName + "/mapper/");
            if (!output.exists()) {
                output.mkdirs();
            }
            File outFile = new File(outputF + "/resources/" + pathName + "/mapper/" + className + "Mapper.xml");
            if (outFile.exists()) {
                outFile.delete();
            }
            OutputStream outputStream = new FileOutputStream(outFile);
            OutputStreamWriter outWriter = new OutputStreamWriter(outputStream);
            if (template != null) {
                template.merge(context, outWriter);
                outWriter.flush();
                outWriter.close();
            }

            System.out.println("success gen mysql");
            Template templateMapper = ve.getTemplate("template/template-mapper");
            File output1 = new File(outputF + "/java/" + pathName + "/mapper/");
            if (!output1.exists()) {
                output1.mkdirs();
            }
            File mapperOutFile = new File(outputF + "/java/" + pathName + "/mapper/" + className + "Mapper.java");
            if (mapperOutFile.exists()) {
                mapperOutFile.delete();
            }
            OutputStream outputMapperStream = new FileOutputStream(mapperOutFile);
            OutputStreamWriter outMapperWriter = new OutputStreamWriter(outputMapperStream);
            if (templateMapper != null) {
                templateMapper.merge(context, outMapperWriter);
                outMapperWriter.flush();
                outMapperWriter.close();
            }
            System.out.println("success gen mapper");

            Template templateModel = ve.getTemplate("template/template-model");
            File outputModel = new File(outputF + "/java/" + pathName + "/model/");
            if (!outputModel.exists()) {
                outputModel.mkdirs();
            }

            File modelOutFile = new File(outputF + "/java/" + pathName + "/model/" + className + "Model.java");
            if (modelOutFile.exists()) {
                modelOutFile.delete();
            }
            OutputStream outputModelStream = new FileOutputStream(modelOutFile);
            OutputStreamWriter outModelWriter = new OutputStreamWriter(outputModelStream);
            if (templateModel != null) {
                templateModel.merge(context, outModelWriter);
                outModelWriter.flush();
                outModelWriter.close();
            }
            System.out.println("success gen model");
            context.put("serviceName", className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> queryFieldList(String database, String tableName) {
        try {
            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            String sql = "select * from columns where table_schema = '" + database + "' and table_name='" + tableName + "'";
            ResultSet rs = statement.executeQuery(sql);
            List<Map<String, String>> list = new ArrayList();
            while(rs.next()) {
                Map<String, String> m1 = new HashMap<String, String>();
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName.equals("action") || columnName.equals("actionTime") || columnName.equals("createTime")) {
                    continue;
                }
                String extra = rs.getString("EXTRA");
                if ("auto_increment".equals(extra)) {
                    m1.put("extra", "1");
                } else {
                    m1.put("extra", "0");
                }
                m1.put("extra", extra);
                String methodName = Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1);
                m1.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                if ("auto_increment".equals(extra)) {
                    m1.put("COLUMN_DEFAULT", "0");
                } else {
                    m1.put("COLUMN_DEFAULT", rs.getString("COLUMN_DEFAULT"));
                }
                m1.put("METHOD_NAME", methodName);
                m1.put("DATA_TYPE", rs.getString("DATA_TYPE"));
                list.add(m1);
            }
            rs.close();
            statement.close();
            conn.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Connection getConnection() throws Exception {
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private VelocityEngine getVelocityEngine() throws Exception{
        VelocityEngine ve = new VelocityEngine();
        Properties properties = new Properties();
        String fileDir = this.getClass().getResource("").getFile();
        ve.setProperty(Velocity.RESOURCE_LOADER, "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        //properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,fileDir);
        properties.setProperty(Velocity.ENCODING_DEFAULT, "utf-8");
        properties.setProperty(Velocity.INPUT_ENCODING, "utf-8");
        properties.setProperty(Velocity.OUTPUT_ENCODING, "utf-8");
        ve.init(properties);
        return ve;
    }
}
