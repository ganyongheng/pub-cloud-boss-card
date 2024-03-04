package com.cn.mybitsplus;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

public class MybatisAutoCodeUtils {

    /**
     * <p>
     * MySQL 生成演示
     * </p>
     */
    public static void main(String[] args) {

        //需要生成的表
        String[] tables = new String[]{
                "online_heaven_shop","online_heaven_shop_detail"
        };
        //数据库连接url
        //String url = "jdbc:mysql://10.1.1.120:3306/sass_tracking_history?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai";
        String url = "jdbc:mysql://10.1.1.120:3306/test_heaven?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai";
        //String url = "jdbc:mysql://10.1.1.120:3306/logistic?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai";
        //数据库账号
        String uerName = "devops";
        //数据库密码
        String password = "2wsx#EDC";
        //生成class 的 父包名
        //String parentPackage = "com.sn.online";
        //String parentPackage = "com.cn.offline";
        String parentPackage = "rabb.heaven";

        String controllerPackage = "controller";

        String servicePackage = "service";

        String serviceImplPackage = "service.impl";

        //生成entity类的 包名（继承父包名）
        String entityPackage = "entity";

        //生成mapper类的 包名（继承父包名）
        String mapperPackage = "mapper";

        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();

        //输出的文件夹
        gc.setOutputDir("C://mybatPlus/temp");

        gc.setFileOverride(true);
        gc.setActiveRecord(true);
        gc.setEnableCache(false);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
        gc.setAuthor("ganyongheng");
        gc.setSwagger2(false);

        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        gc.setServiceName("I%sService");
        gc.setServiceImplName("%sServiceImpl");
        gc.setEntityName("%sDo");
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername(uerName);
        dsc.setPassword(password);
        dsc.setUrl(url);
        mpg.setDataSource(dsc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        // 表名生成策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // 需要生成的表
        strategy.setInclude(tables);
        // 此处可以修改表前缀
        //strategy.setTablePrefix("t_");
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent(parentPackage);
        pc.setController(controllerPackage);
        pc.setService(servicePackage);
        pc.setServiceImpl(serviceImplPackage);
        pc.setEntity(entityPackage);
        pc.setMapper(mapperPackage);
        mpg.setPackageInfo(pc);

        // 执行生成
        mpg.execute();

}
}

