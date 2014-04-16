
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LogoPanel', {
		
    extend: 'Ext.toolbar.Toolbar',//panel.Panel',
    alias: 'widget.logo_panel',

	id: 'DSS_LogoPanel',
	frame: false,
	layout: 'absolute',
	header: false,
	dock: 'top',
	collapsible: true,
	animCollapse: false,
	collapsed: false,
	height: 0, // start closed...otherwise use...DSS_LogoPanelHeight,
//	height: DSS_LogoPanelHeight,
//	bodyStyle: 'background-color:rgb(220,230,240)',
 	overflowY: 'hidden',
	
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
			items: [{
				xtype: 'image',
				width: 399,
				x: 0,
				y: 0,
				src: 'app/images/dss_logo.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.glbrc.org',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			},
			{
				xtype: 'button',
				x: 350,
				y: 16,
				width: 200,
				border: 1,
				scale: 'large',
				text: 'SmartScape DSS Help',
				aURL: 'http://youtu.be/XxZvzqFZTU8',
				handler: function(self) {
					javascript:window.open(self.aURL,'_blank');return false;
				}
			},
			{
				xtype: 'button',
				x: 560,
				y: 16,
				width: 200,
				border: 1,
				text: 'Gratton Lab',
				aURL: 'http://gratton.entomology.wisc.edu',
				scale: 'large',
				handler: function(self) {
					javascript:window.open(self.aURL,'_blank');return false;
				}
			},
			{
				xtype: 'button',
				x: 770,
				y: 16,
				width: 200,
				border: 1,
				scale: 'large',
				text: 'WEI Homepage',
				aURL: 'https://energy.wisc.edu',
				handler: function(self) {
					javascript:window.open(self.aURL,'_blank');return false;
				}
			}]
        });
        
        me.callParent(arguments);
    }
    
});
