package ma.enset.strategies;

import ma.enset.initializers.Initializer;
import ma.enset.injectors.Injector;
import ma.enset.repository.Context;
import ma.enset.resolvers.UnresolvedBean;
import ma.enset.resolvers.UnresolvedBeanException;
import ma.enset.scanners.AnnotationScanner;
import ma.enset.scanners.DetectedBean;
import ma.enset.scanners.Scanner;

import java.util.Optional;
import java.util.Set;


public class AnnotationStrategy implements InjectionStrategy{

    private final Scanner annotationScanner;

    public AnnotationStrategy(String ...forPackages) {
        this.annotationScanner = new AnnotationScanner(forPackages);
    }


    @Override
    public void apply(){
        Set<DetectedBean> detectedBeanSet = annotationScanner.scan();

        int oldSize = 0;
        while (Context.INSTANCE.size() != detectedBeanSet.size()){
            detectedBeanSet.stream()
                    .filter(detectedBean -> detectedBean.getInitializer().canBeInitialized())
                    .forEach(detectedBean ->  {
                        Object bean = detectedBean.getInitializer().initialize();
                        Context.INSTANCE.saveBean(detectedBean.getSpecifiedName(), bean);
                    });

            if (oldSize == Context.INSTANCE.size()){
                detectedBeanSet.stream()
                        .map(DetectedBean::getInitializer)
                        .filter(initializer -> !initializer.canBeInitialized())
                        .map(Initializer::findFirstUnresolvedBean)
                        .findAny()
                        .ifPresent(unresolvedBean -> {
                            throw new UnresolvedBeanException(unresolvedBean);
                        });
            }
            oldSize = Context.INSTANCE.size();
        }

        Optional<UnresolvedBean> unresolvedBean = detectedBeanSet.stream()
                .flatMap(detectedBean -> detectedBean.getInjectorSet().stream())
                .filter(injector -> !injector.canBeInjected())
                .map(Injector::findFirstUnresolvedBean)
                .findAny();

        if (unresolvedBean.isPresent()){
            throw new UnresolvedBeanException(unresolvedBean.get());
        }

        detectedBeanSet.stream()
                .filter(detectedBean -> !detectedBean.getInjectorSet().isEmpty())
                .forEach(detectedBean -> {
                    detectedBean.getInjectorSet()
                            .forEach(Injector::inject);
                });



    }
}
