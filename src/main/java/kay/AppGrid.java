package kay;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppGrid<T> extends Grid<T> {
    private Map<String, HasValueFilter> filters = new HashMap<>();
    private HeaderRow filterRow;
    public AppGrid(Class beanType) {
        super(beanType);
    }

    public ComboBox addComboBoxFilter(String columnId, List items) {
        ComboBox comboBox = new ComboBox();
        comboBox.setStyleName(ValoTheme.COMBOBOX_TINY);
        comboBox.setItems(items);
        if (filterRow == null)
            filterRow = appendHeaderRow();
        filterRow.getCell(columnId).setComponent(comboBox);
        getColumn(columnId).setMinimumWidth(comboBox.getWidth());
        comboBox.addValueChangeListener(event -> {
            filters.get(columnId).setValue(event.getValue() != null ? event.getValue().toString() : null);
            applyFilters();
        });
        filters.put(columnId,new HasValueFilter(comboBox,HasValueFilter.Type.EQUALS,false));
        return comboBox;
    }
    public void addTextFilter(String columnId, HasValueFilter.Type ftype){
        addTextFilter(columnId,ftype,false,"150");
    }

    public void addTextFilter(String columnId, HasValueFilter.Type ftype,boolean isIgnoreCase){
        addTextFilter(columnId,ftype,isIgnoreCase,"150");
    }

    public void addTextFilter(String columnId,HasValueFilter.Type ftype, boolean isIgnoreCase, String width){
        TextFieldWithClear textField = new TextFieldWithClear()
                .withWidth(width)
                .setStyle(ValoTheme.TEXTFIELD_TINY)
                .withChangeListener(event -> {
                    filters.get(columnId).setValue(event != null ? event.getValue().toString() : null);
                    applyFilters();
                });

        if (filterRow == null)
            filterRow = appendHeaderRow();
        filterRow.getCell(columnId).setComponent(textField);
        getColumn(columnId).setMinimumWidth(textField.getWidth());
        filters.put(columnId,new HasValueFilter(textField, ftype,isIgnoreCase));

    }

    private void applyFilters(){
        ((ListDataProvider<T>) getDataProvider()).clearFilters();
        filters.forEach((k,v) -> {
            if (v.getValue() != null && !v.getValue().equals(""))
            ((ListDataProvider<T>) getDataProvider())
                    .addFilter(( ValueProvider<T, ?>)this.getColumn(k).getValueProvider(),
                            field -> v.match(field != null ? field.toString() : null));
        });

    }




    public void clearAllFilers(){
        filters.forEach((k,v) -> v.getComponent().clear());
    }

    public void setEnabledFilters(boolean isEnabled){
        filterRow.getComponents().forEach(c -> c.setEnabled(isEnabled));
    }

}
