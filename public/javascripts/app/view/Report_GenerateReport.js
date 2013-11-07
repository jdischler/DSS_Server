/*
 * File: app/view/ReportTools.js
 */

Ext.define('MyApp.view.Report_GenerateReport', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_generate_report',

    height: 60,
    width: 300,
    title: 'Save/Print Results',
	icon: 'app/images/new_icon.png',
    activeTab: 0,

    layout: 'absolute',
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
			items: [{
				xtype: 'button',
				text: 'Save Results',
				icon: 'app/images/save_small_icon.png',
				x: 100,
				y: 10,
				handler: function() {
					me.askSaveResults();
				}
			},{
				xtype: 'button',
				text: 'Print Results',
				icon: 'app/images/print_small_icon.png',
				x: 250,
				y: 10
			}]
        });

        me.callParent(arguments);
    }/*,

    //--------------------------------------------------------------------------
    askSaveResults: function() {
//    	Ext.Msg.prompt('Save As:', 'Please name this scenario:', func
    },
    
    //--------------------------------------------------------------------------
    saveResults: function(name) {
  /*
		var requestData = {
			clientID: 1234, //temp
			name: name
		};
		
		var clientID_cookie = Ext.util.Cookies.get('DSS_clientID');
		if (clientID_cookie) {
			requestData.clientID = clientID_cookie;
		}
		else {
			console.log('WARNING: no client id cookie was found...');
		}
    	
//		var button = Ext.getCmp('DSS_runModelButton');
//		button.setIcon('app/images/spinner_16a.gif');
//		button.setDisabled(true);
		
		var self = this;
		var obj = Ext.Ajax.request({
			url: location.href + 'saveScenario',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				try {
					var obj= JSON.parse(response.responseText);
					console.log("success: ");
					console.log(obj);
					var newRequest = requestData;
					newRequest.scenarioID = obj.scenarioID;
					self.submitModel(newRequest);
				}
				catch(err) {
					console.log(err);
				}
			},
			
			failure: function(respose, opts) {
//				button.setIcon('app/images/go_icon.png');
//				button.setDisabled(false);
				alert("Scenario save failed, request timed out?");
			}
		});
    }*/

});