package com.pudge

import android.util.Log
import com.pudge.Caller.TAG
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findMethodExactIfExists
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object Util {
    /**
    * 查找所有满足要求的方法
    *
    * @param clazz 该方法所属的类
    * @param returnType 该方法的返回类型
    * @param parameterTypes 该方法的参数类型
    */
    @JvmStatic fun findMethodsByExactParameters(clazz: Class<*>, returnType: Class<*>?, vararg parameterTypes: Class<*>): List<Method> {
        return clazz.declaredMethods.filter { method ->
            if (returnType != null && returnType != method.returnType) {
                return@filter false
            }

            val methodParameterTypes = method.parameterTypes
            if (parameterTypes.size != methodParameterTypes.size) {
                return@filter false
            }
            for (i in parameterTypes.indices) {
                if (parameterTypes[i] != methodParameterTypes[i]) {
                    return@filter false
                }
            }

            method.isAccessible = true
            return@filter true
        }
    }

    /**
     * 查找一个确定的方法, 如果不存在返回 null
     */
    @JvmStatic fun findMethodExactIfExists(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method? =
        try { findMethodExact(clazz, methodName, *parameterTypes) } catch (_: Throwable) { null }

    /**
     * 根据 JVM Specification 生成一个参数签名
     */
    @JvmStatic private fun getParametersString(vararg clazzes: Class<*>): String =
        "(" + clazzes.joinToString(","){ it.canonicalName ?: "" } + ")"


    /**
     * 查找一个确定的方法, 如果不存在, 抛出 [NoSuchMethodException] 异常
     *
     * @param clazz 该方法所属的类
     * @param methodName 该方法的名称
     * @param parameterTypes 该方法的参数类型
     */
    @JvmStatic fun findMethodExact(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        val fullMethodName = "${clazz.name}#$methodName${getParametersString(*parameterTypes)}#exact"
        try {
            return clazz.getDeclaredMethod(methodName, *parameterTypes).apply {
                isAccessible = true
            }
        } catch (e: NoSuchMethodException) {
            throw NoSuchMethodError(fullMethodName)
        }
    }

    /**
     * 执行回调函数, 无视它抛出的任何异常
     */
    @JvmStatic inline fun <T: Any>trySilently(func: () -> T?): T? {
        return try { func() } catch (t: Throwable) { null }
    }

    /**
     * 执行回调函数, 将它抛出的异常记录到 Xposed 的日志里
     */
    @JvmStatic inline fun <T: Any>tryVerbosely(func: () -> T?): T? {
        return try { func() } catch (t: Throwable) {
            Log.e("Xposed", Log.getStackTraceString(t)); null
        }
    }

    /**
     * 异步执行回调函数, 将它抛出的记录到 Xposed 的日志里
     *
     * WARN: 别忘了任何 UI 操作都必须使用 runOnUiThread
     */
    @JvmStatic inline fun tryAsynchronously(crossinline func: () -> Unit): Thread {
        return thread(start = true) { func() }.apply {
            setUncaughtExceptionHandler { _, t ->
                Log.e("Xposed", Log.getStackTraceString(t))
            }
        }
    }

    /**
     * 查找一个确定的成员变量, 如果不存在返回 null
     */
    @JvmStatic fun findFieldIfExists(clazz: Class<*>, fieldName: String): Field? =
        try { clazz.getField(fieldName) } catch (_: Throwable) { null }

    /**
     * 查找指定类中所有特定类型的成员变量
     */
    @JvmStatic fun findFieldsWithType(clazz: Class<*>, typeName: String): List<Field> {
        return clazz.declaredFields.filter {
            it.type.name == typeName
        }
    }

    /**
     * 查找指定类中所有特定泛型的成员变量
     */
    @JvmStatic fun findFieldsWithGenericType(clazz: Class<*>, genericTypeName: String): List<Field> {
        return clazz.declaredFields.filter {
            it.genericType.toString() == genericTypeName
        }
    }

    /**
     * 钩住一个类中所有的方法, 一般只用于测试
     */
    @JvmStatic fun hookAllMethodsInClass(clazz: Class<*>, callback: XC_MethodHook) {
        clazz.declaredMethods.forEach { method -> XposedBridge.hookMethod(method, callback) }
    }
}