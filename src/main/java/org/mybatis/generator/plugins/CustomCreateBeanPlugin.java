/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 功能描述:
 * <p/>
 *
 * @author chaoshibin 新增日期：2017/12/6
 * @author chaoshibin 修改日期：2017/12/6
 * @version 1.0.0
 * @since 1.0.0
 */
public class CustomCreateBeanPlugin extends PluginAdapter {

    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType daoType;
    private FullyQualifiedJavaType interfaceType;
    private FullyQualifiedJavaType serviceInterfaceType;
    private FullyQualifiedJavaType abstractServiceType;
    private FullyQualifiedJavaType extendServiceInterfaceType;
    private FullyQualifiedJavaType extendAbstractServiceType;
    private FullyQualifiedJavaType extendMapperType;

    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType pojoCriteriaType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType service;
    private FullyQualifiedJavaType returnType;
    private String servicePack;
    private String serviceImplPack;
    private String project;
    private String pojoUrl;
    private List<JavaElement> javaElements;
    /**
     * 所有的方法
     */
    private List<Method> methods;
    /**
     * 是否添加注解
     */
    private boolean enableAnnotation = true;


    public CustomCreateBeanPlugin() {
        super();
        // default is slf4j
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
        methods = new ArrayList<Method>();
        javaElements = new ArrayList<JavaElement>();
    }

    /**
     * 读取配置文件
     */
    @Override
    public boolean validate(List<String> warnings) {

        this.servicePack = properties.getProperty("targetPackage");
        this.serviceImplPack = properties.getProperty("implementationPackage");
        this.project = properties.getProperty("targetProject");
        this.pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();

        if (this.enableAnnotation) {
            autowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
            service = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
        }
        return true;
    }

    /**
     * 入口程序
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        // 取Service名称【com.jrq.service.PetService】
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.pojoUrl + ".", "");

        interfaceType = new FullyQualifiedJavaType(servicePack + "." + tableName + "Service");

        serviceInterfaceType = new FullyQualifiedJavaType(servicePack + "." + "Service");
        abstractServiceType = new FullyQualifiedJavaType(servicePack + "." + "AbstractService");
        extendServiceInterfaceType = new FullyQualifiedJavaType(servicePack + "." + "Service<" + tableName + "," + tableName + "Example>");
        extendAbstractServiceType = new FullyQualifiedJavaType("AbstractService<" + tableName + "," + tableName + "Example>");
        extendMapperType = new FullyQualifiedJavaType("IMapper<" + tableName + "," + tableName + "Example>");

        // 【com.jrq.mapper.PetMapper】
        daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());

        // 【com.jrq.service.impl.PetServiceImpl】logger.info(toLowerCase(daoType.getShortName()));
        serviceType = new FullyQualifiedJavaType(serviceImplPack + "." + tableName + "ServiceImpl");

        // 【com.jrq.domain.Pet】
        pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);

        // 【com.jrq.domain.Criteria】
        pojoCriteriaType = new FullyQualifiedJavaType(pojoUrl + "." + tableName + "Example");

        createMapper(files);
        createService(files);
        createServiceImpl(introspectedTable, tableName, files);

        for (JavaElement javaElement : javaElements) {
            addComment(javaElement);
        }
        return files;
    }

    private void createMapper(List<GeneratedJavaFile> files) {
        Interface mapperInterface = new Interface(daoType);
        javaElements.add(mapperInterface);

        mapperInterface.addSuperInterface(extendMapperType);
        mapperInterface.setVisibility(JavaVisibility.PUBLIC);
        mapperInterface.addImportedType(pojoType);
        mapperInterface.addImportedType(pojoCriteriaType);
        GeneratedJavaFile file = new GeneratedJavaFile(mapperInterface, project, context.getJavaFormatter());
        files.add(file);
    }

    private void createService(List<GeneratedJavaFile> files) {
        Interface interfaces = new Interface(interfaceType);
        javaElements.add(interfaces);
        interfaces.addImportedType(pojoType);
        interfaces.addImportedType(pojoCriteriaType);
        interfaces.addImportedType(serviceInterfaceType);

        interfaces.setVisibility(JavaVisibility.PUBLIC);
        interfaces.addSuperInterface(extendServiceInterfaceType);
        GeneratedJavaFile file = new GeneratedJavaFile(interfaces, project, context.getJavaFormatter());
        files.add(file);
    }

    private void createServiceImpl(IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        TopLevelClass topLevelClass = new TopLevelClass(serviceType);
        javaElements.add(topLevelClass);
        topLevelClass.addImportedType(daoType);
        topLevelClass.addImportedType(interfaceType);
        topLevelClass.addImportedType(pojoType);
        topLevelClass.addImportedType(pojoCriteriaType);
        topLevelClass.addImportedType(abstractServiceType);

        if (enableAnnotation) {
            topLevelClass.addImportedType(service);
            topLevelClass.addImportedType(autowired);

            topLevelClass.addAnnotation("@Service");
            topLevelClass.addImportedType(service);
        }

        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.setSuperClass(extendAbstractServiceType);
        topLevelClass.addSuperInterface(interfaceType);

        addField(topLevelClass, tableName);
        topLevelClass.addMethod(addMapper(introspectedTable, tableName));

        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }

    /**
     * add mapper
     */
    protected Method addMapper(IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.setName("getMapper");
        method.setReturnType(new FullyQualifiedJavaType(tableName + "Mapper"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return mapper;");
        method.addBodyLine(sb.toString());
        method.addAnnotation("@Override");
        return method;
    }

    /**
     * add field
     *
     * @param topLevelClass
     * @param tableName
     */
    protected void addField(TopLevelClass topLevelClass, String tableName) {
        Field field = new Field();

        field.setName("mapper");
        topLevelClass.addImportedType(daoType);
        field.setType(daoType);
        field.setVisibility(JavaVisibility.PRIVATE);
        if (enableAnnotation) {
            field.addAnnotation("@Autowired");
        }
        topLevelClass.addField(field);
    }

    /**
     * 注释模版
     *
     * @param field
     */
    public static void addComment(JavaElement field) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sdf.format(new Date());
        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * @author Mybatis Generator ");
        sb.append(date);
        field.addJavaDocLine(sb.toString());
        field.addJavaDocLine(" */");
    }
}
