
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Dev_Popup', {
    extend: 'Ext.window.Window',

    height: 140,
    width: 400,
    title: 'Developer Links',
	icon: 'app/images/tool.png',
    
    layout: 'absolute',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    maximizable: false,
    resizable: false,
    modal: true,

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 20,
				y: 40,
				width: 280,
				html: '<a href="/app/file/DSS_Setup.pdf" download="DSS_Setup.pdf">&#8226 Developer Setup Documentation (PDF, 283 KB)</a>'
			},{
				xtype: 'label',
				x: 20,
				y: 20,
				width: 280,
				html: '<a href="https://github.com/jdischler/DSS_Server">&#8226 SmartScape&#8482 DSS Source Code on GitHub</a>'
			},{
				xtype: 'label',
				x: 20,
				y: 60,
				width: 280,
				html: '<a href="/app/file/DSS_LayerData.zip" download="DSS_LayerData.zip">&#8226 Layer Data (Compressed Zip, 257 MB)</a>'
			}]
        });

        me.callParent(arguments);
    }
    
});


