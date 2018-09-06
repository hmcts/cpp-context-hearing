package uk.gov.moj.cpp.pojoplugin;

import static com.squareup.javapoet.CodeBlock.of;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.generation.pojo.dom.ClassDefinition;
import uk.gov.justice.generation.pojo.dom.Definition;
import uk.gov.justice.generation.pojo.plugin.FactoryMethod;
import uk.gov.justice.generation.pojo.plugin.PluginContext;
import uk.gov.justice.generation.pojo.plugin.classmodifying.ClassModifyingPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class MutablePojoPlugin implements ClassModifyingPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MutablePojoPlugin.class.getName());

    @FactoryMethod
    public static MutablePojoPlugin newMutablePojoPlugin() {
        return new MutablePojoPlugin();
    }

    @Override
    public TypeSpec.Builder generateWith(
            final TypeSpec.Builder classBuilder,
            final ClassDefinition classDefinition,
            final PluginContext pluginContext) {

        final ClassName className = pluginContext
                .getClassNameFactory()
                .createClassNameFrom(classDefinition);

        applyHackToRemoveFinalModifiersFromFields(classBuilder);

        for (final Definition field : classDefinition.getFieldDefinitions()) {

            final TypeName name = pluginContext.getClassNameFactory().createTypeNameFrom(field, pluginContext);

            final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(generateMethodName(field))
                    .addModifiers(PUBLIC)
                    .addParameter(name, field.getFieldName())
                    .returns(className)
                    .addCode("this." + field.getFieldName() + " = " + field.getFieldName() + ";\n")
                    .addCode(of("return this;\n"));

            classBuilder.addMethod(methodBuilder.build());
        }

        return classBuilder;
    }

    private void applyHackToRemoveFinalModifiersFromFields(final TypeSpec.Builder classBuilder) {
        try {
            final Field fieldSpecs = TypeSpec.Builder.class.getDeclaredField("fieldSpecs");
            fieldSpecs.setAccessible(true);

            for (final FieldSpec fieldSpec : (List<FieldSpec>) fieldSpecs.get(classBuilder)) {

                final Field modifiers = FieldSpec.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                removeFinalModifierFromField(modifiers);
                modifiers.set(fieldSpec, Collections.unmodifiableSet(new LinkedHashSet<>(asList(PRIVATE))));

            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void removeFinalModifierFromField(final Field modifiers) throws NoSuchFieldException, IllegalAccessException {
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(modifiersField, modifiers.getModifiers() & ~Modifier.FINAL);
    }

    private String generateMethodName(final Definition field) {
        return "set" + field.getFieldName().substring(0, 1).toUpperCase() + field.getFieldName().substring(1);
    }

}