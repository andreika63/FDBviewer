package kay;


import com.vaadin.data.HasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Optional;


public class TextFieldWithClear extends CustomComponent implements HasValue<String>{
    private final TextField textField = new TextField();
    private final Button clearTextBtn = new Button(VaadinIcons.CLOSE);
    private HasValue.ValueChangeListener listener;

    public TextFieldWithClear() {
        textField.addValueChangeListener(e -> clearTextBtn.setEnabled(e.getValue().length()>0));
        clearTextBtn.setEnabled(false);
        clearTextBtn.addClickListener(c-> {
            textField.clear();
            clearTextBtn.setEnabled(false);
            listener.valueChange(null);
        });
        clearTextBtn.setDescription("очистить");

        final CssLayout layout = new CssLayout();
        layout.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        layout.addComponents(textField,clearTextBtn);
        setCompositionRoot(layout);
    }

    public TextFieldWithClear(String inputPrompt){
        this();
        textField.setPlaceholder(inputPrompt);
    }

    public TextFieldWithClear(String inputPrompt,HasValue.ValueChangeListener listener){
        this(inputPrompt);
        this.listener = listener;
        textField.addValueChangeListener(listener);
    }

    public TextFieldWithClear withWidth(String width){
        textField.setWidth(width);
        return this;
    }

    public TextFieldWithClear withChangeListener(HasValue.ValueChangeListener<String> listener){
        this.listener = listener;
        textField.addValueChangeListener(listener);
        return this;
    }

    public TextFieldWithClear withInputPrompt(String inputPrompt){
        textField.setPlaceholder(inputPrompt);
        return this;
    }
    public TextFieldWithClear setStyle(String style){
            this.textField.setStyleName(style);
            this.clearTextBtn.setStyleName(style);
        return this;
    }

    public TextFieldWithClear withValueChangeTimeout(int timeout){
        this.textField.setValueChangeTimeout(timeout);
        return this;
    }

    @Override
    public void setValue(String value) {
        textField.setValue(value);
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<String> listener) {
        return textField.addValueChangeListener(listener);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        textField.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return textField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return textField.isRequiredIndicatorVisible();
    }

    @Override
    public String getEmptyValue() {
        return textField.getEmptyValue();
    }

    @Override
    public Optional<String> getOptionalValue() {
        return textField.getOptionalValue();
    }

    @Override
    public boolean isEmpty() {
        return textField.isEmpty();
    }

    @Override
    public void clear() {
        textField.clear();
    }
}

