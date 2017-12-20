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

import com.google.common.base.CaseFormat;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述:
 * <p/>
 *
 * @author CHAO 新增日期：2017/12/13
 * @author CHAO 修改日期：2017/12/13
 * @version 1.0.0
 * @since 1.0.0
 */
public class CustomSerializablePlugin extends SerializablePlugin {
    private volatile FullyQualifiedJavaType serializable;
    private volatile FullyQualifiedJavaType apiModel;
    private volatile FullyQualifiedJavaType apiModelProperty;
    private volatile FullyQualifiedJavaType data;
    private boolean swagger = false;
    private boolean lombok = false;

    public CustomSerializablePlugin() {
        super();
        serializable = new FullyQualifiedJavaType("java.io.Serializable");
        apiModel = new FullyQualifiedJavaType("io.swagger.annotations.ApiModel");
        apiModelProperty = new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty");
        data = new FullyQualifiedJavaType("lombok.Data");
    }


    /**
     * 初始化配置
     */
    private void initCondition() {
        swagger = Boolean.parseBoolean(properties.getProperty("swagger"));
        lombok = Boolean.parseBoolean(properties.getProperty("lombok"));
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType(serializable);
        makeSerializableInnerClass(topLevelClass, introspectedTable);
        CustomCreateBeanPlugin.addComment(topLevelClass);

        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("Criteria".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
                innerClass.addSuperInterface(serializable);
                makeSerializableInnerClass(innerClass, introspectedTable);
            }
            if ("Criterion".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
                innerClass.addSuperInterface(serializable);
                makeSerializableInnerClass(innerClass, introspectedTable);
            }
        }
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        initCondition();
        if (swagger) {
            topLevelClass.addImportedType(apiModel);
            topLevelClass.addImportedType(apiModelProperty);
            topLevelClass.addAnnotation("@ApiModel");
            List<Field> fields = topLevelClass.getFields();
            for (Field field : fields) {
                CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "CONSTANT_NAME");
                String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                String value = introspectedTable.getColumn(columnName).getRemarks();
                field.addAnnotation("@ApiModelProperty(\"" + value + "\")");
            }
        }
        if (lombok) {
            topLevelClass.addImportedType(data);
            topLevelClass.addAnnotation("@Data");
        }
        topLevelClass.addImportedType(serializable);
        makeSerializableInnerClass(topLevelClass, introspectedTable);
        return true;
    }

    public static void main(String[] args) {
        String id = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "userId");
        System.out.println(id);
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
    }

    protected void makeSerializableInnerClass(InnerClass innerClass, IntrospectedTable introspectedTable) {

        innerClass.addSuperInterface(this.serializable);
        Field field = new Field();
        field.setFinal(true);
        field.setStatic(true);
        field.setInitializationString("1L");
        field.setName("serialVersionUID");
        field.setType(new FullyQualifiedJavaType("long"));
        field.setVisibility(JavaVisibility.PRIVATE);
        this.context.getCommentGenerator().addFieldComment(field, introspectedTable);
        List<Field> fields = innerClass.getFields();
        List<Field> fieldList = new ArrayList<Field>();
        fieldList.add(field);
        fieldList.addAll(fields);
        fields.clear();
        fields.addAll(fieldList);
    }
}
