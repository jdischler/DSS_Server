
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LogoPanel', {
		
    extend: 'Ext.panel.Panel',
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
	bodyStyle: 'background-color:rgb(220,230,240)',
	
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
				xtype: 'image',
				x: 375,
				y: 10,
				src: 'app/images/dss_home_button.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.youtube.com',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			},
			{
				xtype: 'image',
				x: 575,
				y: 10,
				src: 'app/images/dss_help_button.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.facebook.com',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			},
			{
				xtype: 'image',
				x: 775,
				y: 10,
				src: 'app/images/dss_help_button.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.google.com',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			}]
        });
        
        me.callParent(arguments);
    }
    
});
