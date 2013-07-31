
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LogoPanel', {
		
    extend: 'Ext.panel.Panel',
    alias: 'widget.logo_panel',

	id: 'DSS_LogoPanel',
	frame: false,
	layout: {
		type: 'hbox',
		pack: 'center',
		align: 'stretch'
	},
	header: false,
	dock: 'top',
	collapsible: true,
	animCollapse: false,
	collapsed: false,
	height: 0, // start closed...otherwise use...DSS_LogoPanelHeight,
	bodyStyle: 'background-color:rgb(220,230,240)',
	
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
			items: [{
				xtype: 'image',
				width: 356,
				src: 'app/images/dss_logo.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.glbrc.org',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			},
			{
				xtype: 'image',
				flex: 1,
				src: 'app/images/globe_icon.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.glbrc.org',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			},
			{
				xtype: 'image',
				flex: 1,
				src: 'app/images/globe_icon.png',
				autoEl: {
					tag: 'a',
					href: 'http://www.glbrc.org',
					onclick: "javascript:window.open(this.href,'_blank');return false;"	
				}
			}]
        });
        
        me.callParent(arguments);
    }
    
});
