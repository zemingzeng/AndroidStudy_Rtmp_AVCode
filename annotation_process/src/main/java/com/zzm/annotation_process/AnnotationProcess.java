package com.zzm.annotation_process;

import com.google.auto.service.AutoService;
import com.zzm.router_annotation.Router;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class) //注册当前类为注解处理器
public class AnnotationProcess extends AbstractProcessor {

    //生成java class 文件类
    private Filer filer;
    //存放注解值和全类名的map
    private final Map<String, String> map = new HashMap<>();
    //打印工具
    private Messager messager;

    private void print(String s) {
        messager.printMessage(Diagnostic.Kind.NOTE, s);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        print("---------------AnnotationProcess init-----------");
    }

    /**
     * 支持的java最新版本
     * 如果不重写此方法又知道当前java版本则在这个类上打上SupportedSourceersion注解就行
     *      *V
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        print("---------AnnotationProcess getSupportedSourceVersion:" + processingEnv.getSourceVersion() + "--------------");
        return processingEnv.getSourceVersion();
    }

    /**
     * 收集哪些注解（当然是处理自己写的注解）
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        print("------------AnnotationProcess getSupportedAnnotationTypes----------------");
        Set<String> set = new HashSet<>();
        //注解全类名
        set.add(Router.class.getCanonicalName());
        return set;
    }

    /**
     * 拿到自己想要的注解的类集合并且处理
     * 每个模块会生成一个对应文件
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        print("--------------AnnotationProcess process---------------------");

        //拿到当前“模块”getSupportedAnnotationTypes()方法返回的想要处理的注解标记的target
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(Router.class);
        print("AnnotationProcess process elementsAnnotatedWith size:" + elementsAnnotatedWith.size());
        for (Element e : elementsAnnotatedWith) {
            //full class name
            String fullClassName = ((TypeElement) e).getQualifiedName().toString();
            print("AnnotationProcess process fullClassName:" + fullClassName);
            String RouterAnnotationValue = ((Router) ((TypeElement) e).getAnnotation(Router.class)).RouterActivity();
            print("AnnotationProcess process RouterAnnotationValue:" + RouterAnnotationValue);
            map.put(RouterAnnotationValue, fullClassName );
        }

        if (map.size() > 0) {
            //要生成的java文件
            JavaFileObject sourceFile;
            Writer writer = null;
            try {
                String className = "RouterActivityUtil" + System.currentTimeMillis();
                String packageName = "com.zzm.annotation_process.";
                String fullClassName = packageName + className;
                sourceFile = filer.createSourceFile(fullClassName);
                writer = sourceFile.openWriter();

                StringBuilder str = new StringBuilder();
                str.append("package com.zzm.annotation_process;\n");
                str.append("import com.zzm.router.IRouter;\n");
                str.append("import com.zzm.router.Router;\n");
                str.append("public class ").append(className).append(" ")
                        .append("implements IRouter {\n");
                str.append("@Override\n");
                str.append("public void putRouterActivity() {\n");

                for (Entry<String, String> entry : map.entrySet()) {
                    print("map.entrySet for each");
                    String value = entry.getValue();
                    String key = entry.getKey();
                    str.append("Router.SingleTonHolder.getInstance().putRouterActivity(");
                    str.append("\"").append(key).append("\"").append(",")
                            .append("\"").append(value).append("\"");
                    str.append(");\n");
                }
                str.append("}\n");
                str.append("}\n");

                writer.write(str.toString());

                map.clear();

            } catch (IOException e) {
                e.printStackTrace();
                print(e.toString());
            } finally {
                try {
                    if (null != writer) {
                        writer.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    print(e.toString());
                }
            }

        }
        return false;
    }


//    package com.zzm.annotation_process;
//    import com.zzm.router.IRouter;
//    import com.zzm.router.Router;
//    public class RouterActivityUtilXXX implements IRouter {
//        @Override
//        public void putRouterActivity() {
//            Router.SingleTonHolder.getInstance().putRouterActivity(key, value);
//            Router.SingleTonHolder.getInstance().putRouterActivity("tag", xxxx.class);
//            Router.SingleTonHolder.getInstance().putRouterActivity("tag", xxxx.class);
//        }
//    }
}