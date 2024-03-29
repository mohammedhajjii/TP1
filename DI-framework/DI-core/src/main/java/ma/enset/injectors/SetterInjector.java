package ma.enset.injectors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import ma.enset.resolvers.BeanResolver;
import ma.enset.resolvers.UnresolvedBean;
import ma.enset.resolvers.UnresolvedBeans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Getter
@Setter
@Builder
public class SetterInjector implements Injector{
    private Method setter;
    private BeanResolver instance;
    private BeanResolver parameter;


    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public void inject() {
        setter.invoke(instance.resolve(), parameter.resolve());
    }

    @Override
    public boolean canBeInjected() {
        return instance.canBeResolved() && parameter.canBeResolved();
    }

    @Override
    public UnresolvedBean findFirstUnresolvedBean() {
        if (!instance.canBeResolved())
            return UnresolvedBeans.from(instance);

        if (!parameter.canBeResolved())
            return UnresolvedBeans.from(parameter);

        return null;
    }
}
