
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LogoPanel', {
		
    extend: 'Ext.toolbar.Toolbar',//panel.Panel',
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
		align: 'stretch'
	},
	header: false,
	dock: 'top',
	collapsible: true,
	animCollapse: false,
	collapsed: false,
	height: DSS_LogoPanelHeight,
 	overflowY: 'hidden',
	
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
        	items: [{
        		xtype: 'container',
        		flex: 1,
        		layout: 'absolute',
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
					width: 150,
					border: 1,
					scale: 'large',
					text: 'SmartScape&#8482 Help', // tm = &#8482;
					aURL: 'http://youtu.be/XxZvzqFZTU8',
					handler: function(self) {
						javascript:window.open(self.aURL,'_blank');return false;
					}
				},
				{
					xtype: 'button',
					x: 510,
					y: 16,
					width: 100,
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
					x: 620,
					y: 16,
					width: 120,
					border: 1,
					scale: 'large',
					text: 'WEI Homepage',
					aURL: 'https://energy.wisc.edu',
					handler: function(self) {
						javascript:window.open(self.aURL,'_blank');return false;
					}
				},
				{
					xtype: 'button',
					x: 750,
					y: 16,
					width: 130,
					border: 1,
					scale: 'large',
					text: 'Developer Links',
					handler: function(self) {
						Ext.create('MyApp.view.Dev_Popup').show();
					}
				}]
			},{
				id: 'DSS_LogoExtraContainer',
				xtype: 'container',
				width: 80,
				layout: 'absolute',
				items: [{
					id: 'DSS_LoginButton',
					xtype: 'button',
					x: 0,
					y: 16,
					width: 70,
					border: 1,
					scale: 'large',
					text: 'Login',
					DSS_LoggedIn: false,
					handler: function(button) {
						if (button.DSS_LoggedIn) {
							me.tryLogout(me, button);
						}
						else {
							var login = Ext.create('MyApp.view.Login_Popup');
							login.DSS_LoginButton = button;
							login.show();
						}
					}
				},
				{
					id: 'DSS_ExtraButton',
					xtype: 'button',
					x: 80,
					y: 16,
					width: 70,
					border: 1,
					scale: 'large',
					DSS_LoggedIn: false,
					text: 'Admin',
					hidden: true,
					handler: function(button) {
						Ext.create('MyApp.view.Access_Popup').show();
					}
				}]
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
				me.hideExtra();
			},
			
			failure: function(response, opts) {
				Ext.MessageBox.alert('Logout Error', response.responseText);
			}
		});
	},
	
    //--------------------------------------------------------------------------
	showExtra: function() {
	
		var x = Ext.getCmp('DSS_LogoExtraContainer');
		x.animate({
			dynamic: true,
			to: {
				width: 160
			},
			listeners: {
				afteranimate: function() {
					Ext.getCmp('DSS_ExtraButton').show();
				}
			}
		});
	},

    //--------------------------------------------------------------------------
    hideExtra: function() {
    
		var x = Ext.getCmp('DSS_LogoExtraContainer');
		Ext.getCmp('DSS_ExtraButton').hide();
		x.animate({
			dynamic: true,
			to: {
				width: 80
			},
		});

    }
});
