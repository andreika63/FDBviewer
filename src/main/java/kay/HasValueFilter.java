package kay;

import com.vaadin.data.HasValue;

public class HasValueFilter {
    private String value;
    private Type type;
    private boolean isIgnoreCase;
    private HasValue component;

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public HasValue getComponent() {
        return component;
    }

    public HasValueFilter(HasValue component, Type type, boolean isIgnoreCase){
        this.component = component;
        this.type = type;
        this.isIgnoreCase = isIgnoreCase;
    }

    public enum Type{EQUALS,STARTSWITH,CONTAINS}

    public boolean match(String field){
        if (field != null){
            if (this.isIgnoreCase){
                field = field.toLowerCase();
                this.value = this.value.toLowerCase();
            }
            switch (this.type){
                case EQUALS: return field.equals(this.value);
                case CONTAINS: return field.contains(this.value);
                case STARTSWITH: return field.startsWith(this.value);
            }
        }
        return false;
    }
}