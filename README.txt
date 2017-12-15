====
       Copyright 2017 the original author or authors.
	   
	   1.读取数据库注释
	   
	   2.创建service层
	   
	   3.在update语句添加空字符串判断
	   
	   在 generatorConfig.xml 添加下列配置
	   
	    <plugin type="org.mybatis.generator.plugins.EqualsHashCodePlugin"/>
        <plugin type="org.mybatis.generator.plugins.ToStringPlugin" />

        <!-- mybatis generator 重写插件 example序列化-->
        <plugin type="org.mybatis.generator.plugins.CustomSerializablePlugin">
            <property name="suppressJavaInterface" value="false"/>
        </plugin>

        <!-- mybatis generator 重写插件 创建service-->
        <plugin type="org.mybatis.generator.plugins.CustomCreateBeanPlugin">
            <property name="targetPackage" value="com.jrq.core.service" />
            <property name="implementationPackage" value="com.jrq.core.service.impl" />
            <property name="targetProject" value="E:\Workspace\USERAPI\userapi-core\src\main\java" />
        </plugin>
        <!-- mybatis generator 重写插件 添加注释-->
        <commentGenerator type="org.mybatis.generator.plugins.CustomCommentGeneratorPlugin">
            <property name="suppressAllComments" value="false"/>
            <property name="suppressDate" value="true"/>
        </commentGenerator>
====

