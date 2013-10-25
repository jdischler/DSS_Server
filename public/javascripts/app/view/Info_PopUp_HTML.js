Ext.define('MyApp.view.Info_PopUp_HTML', {
    extend: 'Ext.window.Window',

    height: 480,
    width: 640,
    title: 'Temp',
	icon: 'app/images/magnify_icon.png',
    layout: 'fit',
    
    initComponent: function() {
        var me = this;
                    
        Ext.applyIf(me, {
            items: [{
            xtype: 'box',
            autoEl: {
                tag: 'iframe',
                src: me.DSS_InfoHTML
            }}]
        });

        me.callParent(arguments);
    }

});


