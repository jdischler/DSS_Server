
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LogoPanel', {
		
    extend: 'Ext.container.Container',//panel.Panel',
    alias: 'widget.logo_panel',

    requires: [
    	'MyApp.view.Dev_Popup',
    	'MyApp.view.Login_Popup',
    	'MyApp.view.Access_Popup'
    ],
    
	id: 'DSS_LogoPanel',
	frame: false,
	layout: {
		type: 'hbox',//'absolute',
		pack: 'start',
		align: 'middle'
	},
	header: false,
	dock: 'top',
	collapsible: true,
	animCollapse: false,
	collapsed: false,
//	height: DSS_LogoPanelHeight,
 	overflowY: 'hidden',
 	style: 'background-color: #d3e1f1',
	
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        Ext.applyIf(me, {
        	items: [{
        		xtype: 'container',
				margins: '0 2 0 0',
				width: 220,
				height: 51,
				html: '<a href="http://www.glbrc.org"><img id="ddd" src="app/images/dss_logo.png" style="width:220px"></a>',
				listeners: {
					afterrender: function(self) {
						Ext.defer(function() {
							self.updateLayout();
						}, 100);
						
					}	
				}
				
			},{
				xtype: 'button',
				margins: 'auto 2',
				border: 1,
				scale: 'large',
				text: 'SmartScape&#8482 Help', // tm = &#8482;
				aURL: 'http://youtu.be/XxZvzqFZTU8',
				handler: function(self) {
					javascript:window.open(self.aURL,'_blank');return false;
				}
			},{
				xtype: 'button',
				margins: 'auto 2',
				border: 1,
				text: 'Gratton Lab',
				aURL: 'http://gratton.entomology.wisc.edu',
				scale: 'large',
				handler: function(self) {
					javascript:window.open(self.aURL,'_blank');return false;
				}
			},{
				xtype: 'button',
				margins: 'auto 2',
				border: 1,
				scale: 'large',
				text: 'WEI Homepage',
				aURL: 'https://energy.wisc.edu',
				handler: function(self) {
					javascript:window.open(self.aURL,'_blank');return false;
				}
			},{
				xtype: 'container',
				flex: 1,
			}]
        });
        
        me.callParent(arguments);
    },
	
    //--------------------------------------------------------------------------
    tryLogout: function(panel, button) {
    
    	var me = panel;
		var obj = Ext.Ajax.request({
			url: location.href + 'logout',
			jsonData: {},
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
			
				var obj= JSON.parse(response.responseText);
				
				for (var t = 0; t < obj.restrictedLayers.length; t++) {
					var restricted = obj.restrictedLayers[t];
					
					console.log(restricted);
					for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
						var layer = DSS_globalQueryableLayers[i];
						if (layer.DSS_QueryTable == restricted) {
							layer.DSS_AccessLocked = true;
							layer.hide();
							layer.header.getComponent('DSS_ShouldQuery').toggle(false);
						}
					}
				}
				button.setText('Login');
				button.DSS_LoggedIn = false;
			},
			
			failure: function(response, opts) {
				Ext.MessageBox.alert('Logout Error', response.responseText);
			}
		});
	}
	
});
