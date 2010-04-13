package nl.queuemanager.smm;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.jms.JMSDomain;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SMMModule extends AbstractModule {

	@Override
	protected void configure() {
//		bindInterceptor(Matchers.inSubpackage("nl.queuemanager"), Matchers.any(), 
//			new MethodInvocationTracingInterceptor());
		
		// We use an Xml file for configuration
		bind(XmlConfiguration.class).in(Scopes.SINGLETON);
		bind(Configuration.class).to(XmlConfiguration.class);
		bind(SMMConfiguration.class).to(XmlConfiguration.class);
		
		// The JMSDomain implementation for SonicMQ
		bind(JMSDomain.class).to(Domain.class).in(Scopes.SINGLETON);
	}

//	public class MethodInvocationTracingInterceptor implements MethodInterceptor {
//		public Object invoke(MethodInvocation invocation) throws Throwable {
//			Class<?> objectClass = invocation.getThis() != null ? 
//				invocation.getThis().getClass() : 
//				invocation.getMethod().getDeclaringClass();
//
//			String declaringClassName = invocation.getMethod().getDeclaringClass().getName();
//				
//			String methodName = String.format("%s::%s", 
//					objectClass.getName(), 
//					invocation.getMethod().getName());
//
//			if(declaringClassName.startsWith("nl.queuemanager"))
//				System.out.println("ENTER " + methodName);
//			
//			Object ret = invocation.proceed();
//			
//			if(declaringClassName.startsWith("nl.queuemanager"))
//				System.out.println("LEAVE " + methodName);
//			
//			return ret;
//		}
//	}
	
}
