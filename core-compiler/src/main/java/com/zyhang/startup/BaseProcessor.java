package com.zyhang.startup;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public abstract class BaseProcessor extends AbstractProcessor {

    private static final String NEWLINE = "\r\n";

    protected Filer filer;
    private Types types;
    private Elements elements;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elements = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    /**
     * 从字符串获取TypeElement对象
     */
    public TypeElement typeElement(String className) {
        return elements.getTypeElement(className);
    }

    /**
     * 从字符串获取TypeMirror对象
     */
    public TypeMirror typeMirror(String className) {
        return typeElement(className).asType();
    }

    /**
     * 从字符串获取ClassName对象
     */
    public ClassName className(String className) {
        return ClassName.get(typeElement(className));
    }

    /**
     * 从字符串获取TypeName对象，包含Class的泛型信息
     */
    public TypeName typeName(String className) {
        return TypeName.get(typeMirror(className));
    }

    public static String getClassName(TypeMirror typeMirror) {
        return typeMirror == null ? "" : typeMirror.toString();
    }

    public boolean isSubType(TypeMirror type, String className) {
        return type != null && types.isSubtype(type, typeMirror(className));
    }

    public boolean isSubType(Element element, String className) {
        return element != null && isSubType(element.asType(), className);
    }

    public boolean isSubType(Element element, TypeMirror typeMirror) {
        return element != null && types.isSubtype(element.asType(), typeMirror);
    }

    /**
     * 非抽象类
     */
    public boolean isConcreteType(Element element) {
        return element instanceof TypeElement && !element.getModifiers().contains(
                Modifier.ABSTRACT);
    }

    /**
     * 非抽象子类
     */
    public boolean isConcreteSubType(Element element, String className) {
        return isConcreteType(element) && isSubType(element, className);
    }

    /**
     * 非抽象子类
     */
    public boolean isConcreteSubType(Element element, TypeMirror typeMirror) {
        return isConcreteType(element) && isSubType(element, typeMirror);
    }

//    public boolean isActivity(Element element) {
//        return isConcreteSubType(element, Const.ACTIVITY_CLASS);
//    }
//
//    public boolean isFragment(Element element) {
//        return isConcreteSubType(element, Const.FRAGMENT_CLASS);
//    }
//
//    public boolean isFragmentV4(Element element) {
//        return isConcreteSubType(element, Const.FRAGMENT_V4_CLASS);
//    }
//
//    public boolean isHandler(Element element) {
//        return isConcreteSubType(element, Const.URI_HANDLER_CLASS);
//    }
//
//    public boolean isInterceptor(Element element) {
//        return isConcreteSubType(element, Const.URI_INTERCEPTOR_CLASS);
//    }

    public static String randomHash() {
        return hash(UUID.randomUUID().toString());
    }

    public static String hash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(str.hashCode());
        }
    }

    protected void log(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args) + NEWLINE);
    }

    protected void warn(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format(format, args) + NEWLINE);
    }

    protected void error(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args) + NEWLINE);
    }
}
