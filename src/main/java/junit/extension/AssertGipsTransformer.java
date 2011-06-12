package junit.extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class AssertGipsTransformer implements ClassFileTransformer{
	
	private ClassPool pool;

	public AssertGipsTransformer() {
		this.pool = ClassPool.getDefault();
	}
	
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        inst.addTransformer(new AssertGipsTransformer());
    }

    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
    	if(className.equals("org/junit/Assert")){
    		try {
    			return this.doAssertTransform(classfileBuffer);
    		} catch (Exception ex) {
    			throw new IllegalClassFormatException(ex.getMessage());
    		}
    	}else if(className.equals("org/junit/internal/runners/statements/InvokeMethod")) {
    		try {
    			return this.doInvokeMethodTransform(classfileBuffer);
    		} catch (Exception ex) {
    			throw new IllegalClassFormatException(ex.getMessage());
    		}
    	}    		
    	return classfileBuffer;
	}

	private byte[] doAssertTransform(byte[] classfileBuffer) throws IOException, RuntimeException, CannotCompileException {
        ByteArrayInputStream istream = new ByteArrayInputStream(classfileBuffer);
        CtClass cc = this.pool.makeClass(istream);
		CtMethod[] methods = cc.getDeclaredMethods();
		for (CtMethod method : methods) {
			if(method.getName().startsWith("assert")){
				method.insertBefore("org.junit.internal.runners.statements.InvokeMethod.called = true;");
			}
		}
        return cc.toBytecode();
	}

	private byte[] doInvokeMethodTransform(byte[] classfileBuffer) throws IOException, RuntimeException, NotFoundException, CannotCompileException {
        ByteArrayInputStream istream = new ByteArrayInputStream(classfileBuffer);
        CtClass cc = this.pool.makeClass(istream);
		CtField called = new CtField(CtClass.booleanType, "called", cc);
		called.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
		cc.addField(called);
		CtMethod method = cc.getDeclaredMethod("evaluate");
		method.insertBefore("org.junit.internal.runners.statements.InvokeMethod.called = false;");
		method.insertAfter("if(org.junit.internal.runners.statements.InvokeMethod.called == false) throw new AssertionError((Object)\"not assert execution.\");");
		return cc.toBytecode();
	}
	
}
