package kay;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

//@Push(transport = Transport.WEBSOCKET_XHR)
@Title("FDB viewer") //заголовок в окне браузера
@Theme("valo") //Тема
@SpringUI
public class AppUI extends UI {
    private AppGrid<FDBentry> grid = new AppGrid(FDBentry.class);
    private TextField filterText = new TextField();
    private Button clearFilterTextBtn = new Button(VaadinIcons.CLOSE);
    private TextField ipAddress = new TextField();
    private Button getFdbBtn = new Button(VaadinIcons.ARROW_DOWN);
    private Button getLLDPBtn = new Button(VaadinIcons.EYE);
    private Button getIfBtn = new Button(VaadinIcons.BOOK);
    private InetAddressValidator iav = InetAddressValidator.getInstance();
    @Autowired
    private SnmpWalk snmpWalk;
    @Value("${snmp.community}")
    private String community;
    private List<FDBentry> data;
    private Label label = new Label();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        root.setMargin(false);
        root.setSpacing(true);
        setContent(root);

        final CssLayout getFdbLayout = new CssLayout();
        getFdbLayout.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        getFdbLayout.addComponents(ipAddress, getFdbBtn,getIfBtn,getLLDPBtn);
        ipAddress.setValue("192.168.1.");
        ipAddress.addValueChangeListener(e -> {
            getFdbBtn.setEnabled(iav.isValidInet4Address(e.getValue()));
            getLLDPBtn.setEnabled(getFdbBtn.isEnabled());
            getIfBtn.setEnabled(getFdbBtn.isEnabled());
        });

        getFdbBtn.setEnabled(false);
        getFdbBtn.setDescription("Download FDB");
        getFdbBtn.addClickListener(e -> update());

        //filterText.setEnabled(false);
        //clearFilterTextBtn.setEnabled(false);
        getLLDPBtn.setEnabled(false);
        getLLDPBtn.setDescription("View LLDP");
        getLLDPBtn.addClickListener(e -> showLLDP());

        getIfBtn.setEnabled(false);
        getIfBtn.setDescription("View Interfaces");
        getIfBtn.addClickListener(e -> showInterfaces());

        clearFilterTextBtn.setDescription("Clear filter");
        filterText.setPlaceholder("row filter...");
        filterText.setWidth(200.0F,Unit.PIXELS);
        filterText.addValueChangeListener(e -> {
            if (data != null)
                grid.setItems(filterData(data, e.getValue().replaceAll("[-:]", "")));
        });
        clearFilterTextBtn.addClickListener(clickEvent -> {
            filterText.clear();
            if (data != null)
                grid.setItems(data);
        });
        grid.setColumns("portNum","portName","portAlias","mac","vlan");
        grid.addTextFilter("portNum", HasValueFilter.Type.CONTAINS,true,"150");
        grid.addTextFilter("portName", HasValueFilter.Type.CONTAINS,true,"150");
        grid.addTextFilter("portAlias", HasValueFilter.Type.CONTAINS,true,"150");
        grid.addTextFilter("mac", HasValueFilter.Type.CONTAINS,true,"150");
        grid.addTextFilter("vlan", HasValueFilter.Type.EQUALS,true,"150");
        grid.setEnabledFilters(false);

        final CssLayout filtering = new CssLayout();
        filtering.setWidth(100.0F, Unit.PERCENTAGE);
        filtering.addComponents(filterText, clearFilterTextBtn);
        filtering.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);


        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setSpacing(true);
        topBar.setWidth(100.0F, Unit.PERCENTAGE);
        label.setSizeUndefined();//по умоланию 100%, topBar.setExpandRatio(filtering,1.0f); перекрывает этот label
        label.addStyleName("align-right");
        topBar.addComponents(getFdbLayout, filtering, label);
        topBar.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);
        topBar.setExpandRatio(filtering, 1.0f);


        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        root.addComponents(topBar, grid);
        root.setExpandRatio(grid, 1.0f);
        //snmpWalk = SnmpWalk.getInstance(); //
    }

    private void showInterfaces() {
        List<IFentry> ifc = snmpWalk.getInterfaces(ipAddress.getValue(), community);
        if (ifc != null){
            Window lldpWindow = new Window("ifMIB: "+ ipAddress.getValue());
            //lldpWindow.setModal(true);
            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            layout.setMargin(true);
            lldpWindow.setContent(layout);
            Grid grid = new Grid();
            grid.setItems(ifc);
            grid.setColumnOrder("ifIndex","ifDescr","ifAdminStatus","ifOperStatus","ifAlias");
            grid.setColumns("ifIndex","ifDescr","ifAdminStatus","ifOperStatus","ifAlias");
            layout.addComponent(grid);
            grid.setSizeFull();
            layout.setExpandRatio(grid,1.0F);
            lldpWindow.setWidth(90.0F, Unit.PERCENTAGE);
            lldpWindow.setHeight(70.0F, Unit.PERCENTAGE);
            lldpWindow.center();
            addWindow(lldpWindow);
        } else Notification.show("ifMIB is not supported on " + ipAddress.getValue());

    }

    private void showLLDP() {
        List<LLDPentry> lldps = snmpWalk.getLLDP(ipAddress.getValue(), community);
        if (lldps != null){
            Window lldpWindow = new Window("LLDP: "+ ipAddress.getValue());
            //lldpWindow.setModal(true);
            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            layout.setMargin(true);
            lldpWindow.setContent(layout);
            Grid grid = new Grid();
            grid.setItems(lldps);
            grid.setColumnOrder("localPortNum","localPortName","localPortAlias","remoteIpAddress","remoteSysName","remotePortName"
                    ,"remotePortAlias","remoteSysDesc");
            layout.addComponent(grid);
            grid.setSizeFull();
            layout.setExpandRatio(grid,1.0F);

            lldpWindow.setWidth(90.0F, Unit.PERCENTAGE);
            lldpWindow.setHeight(70.0F, Unit.PERCENTAGE);

            lldpWindow.center();
            addWindow(lldpWindow);
        } else Notification.show("LLDP is not supported on " + ipAddress.getValue());

        //lldps.forEach(e-> System.out.println(e));
    }


    private void update() {
        //push does not work ;(
        //access(() -> label.setValue("Получаем данные коммутатора " + ipAddress.getValue() + "... "));
        data = snmpWalk.getFDB(ipAddress.getValue(), community);
        grid.clearAllFilers();
        grid.setEnabledFilters(data != null ? data.size() > 0 : false);
        if (data != null && data.size() > 0) {
            grid.setItems(filterData(data, filterText.getValue().replaceAll("[-:]", "")));
            grid.setColumnOrder("portNum", "portName", "portAlias", "mac", "vlan");
            grid.addItemClickListener(e -> Notification.show(Vendor.getVendor(e.getItem().getMac())));
            label.setValue("FDB " + ipAddress.getValue() + " [" + data.size() + "]");
            //filterText.setEnabled(true);
            //clearFilterTextBtn.setEnabled(true);
        } else {
            label.setValue("Данные не получены...");
            //filterText.setEnabled(false);
            //clearFilterTextBtn.setEnabled(false);
        }
    }

    private List<FDBentry> filterData(List<FDBentry> data, String filter) {
        if (filter == null || filter.equals("")) return data;
        ArrayList<FDBentry> result = new ArrayList<>();
        for (FDBentry e : data) {
            if (e.toString().contains(filter.toLowerCase())) result.add(e);
        }
        return result;
    }
}
