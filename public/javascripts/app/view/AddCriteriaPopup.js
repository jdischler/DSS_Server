
//------------------------------------------------------------------------------
Ext.define('MyApp.view.AddCriteriaPopup', {
    extend: 'Ext.window.Window',

    requires: [
    	'MyApp.view.AddCriteriaWidget'
    ],
    
    height: 318,
//    height: 600,
    width: 330,
	layout: {
		type: 'accordion',
	//	animate: false,
		multi: true,
		titleCollapse: false
	},
	modal: true,
//	overflowY: 'scroll',
   // resizable: false,
//	constrainHeader: true,
    closable: false,
//	icon: 'app/images/layers_icon.png',
    header: false,//title: 'Transform and Management Options',
    fbar: [{
    	xtype: 'button',
    	text: 'Help',
    	handler: function(button) {
			Ext.Msg.show({
				title:'Step 1 Help',
				maxWidth: 750,
				maxHeight: 700,
//				msg: 'In Step 1, land is selected based on the <i>intersection</i> of the critera you specify. In Step 2, once you choose land based on your criteria, you can then transform this land into a <i>new</i> landcover type. In Step 3, you request a model simulation to be run and then you will be presented with an analysis of possible impacts of this hypothetical land use change.<br /><br />' +
				msg: '<div style="height: 600px; text-align:justify; padding-right: 30px;">' +
				'<b>Step 1:</b><br /><br />Land is selected based on the <i>intersection</i> of the critera you specify.<br /><br />' +
				'For example, to start, you could ask the question: "I am concerned about water quality; where are erosion prone rowcrops located in the landscape?".<br /><br />' +
				'In the SmartScape software, one way you could think about this question is as the intersection of three things: "Landcover", "Distance to Streams", and "Land Capability Subclass".<br /><br />' +
				'Add these three criteria to your selection, confirm with the "OK" button, you will then be presented with options for specifying the <i>details</i> of your criteria.<br /><br />' +
				'Continuing with this example, we could refine our critiera by choosing details such as these: <br />' +
				'<div style="padding-left: 15px;">' +
				'&#8226; We want rowcrops, so check "Corn" and/or "Soy" for our "Landcover" critera.<br />' +
				'&#8226; We want land close to water, so specify "<= 200 feet" for our "Distance to Stream" critera.<br />' +
				'&#8226; Check "Erosion Prone" for our "Land Capability Subclass" critera.<br />' +
				'</div><br />' +
				'Which looks like this:<br />' +
				'<img src="app/images/step_1_help.png" alt="step 1" width="465" height="275"><br /><br />' +
				'in <b>Step 2</b>, you can transform these specific areas with rowcrops on erosion prone land into a more stable perennial crop.' +
				'</div>',
				buttons: Ext.Msg.OK,
				icon: Ext.Msg.INFO
			});    		
    	}
    },{
    	xtype: 'tbspacer',
    	width: 15
    },{
    	xtype: 'button',
    	text: 'Ok',
    	handler: function(button) {
    		
    		var hasCriteria = false;
			for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
				if (DSS_globalQueryableLayers[i].includeInQuery()) {
					hasCriteria = true;
					break;
				}
			}
			if (hasCriteria) {
				Ext.getCmp('DSS_queryButton').show();
				Ext.getCmp('DSS_ScenarioSummary').show();
				Ext.getCmp('DSS_MainViewport').updateLayout();
			}
			else {
			//	Ext.getCmp('DSS_queryButton').hide();
			//	Ext.getCmp('DSS_ScenarioSummary').hide();
			}
    		button.up().up().close();
    	}
    },{
    	xtype: 'tbspacer',
    	width: 60
    }],
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	xtype: 'panel',
            	hidden: true,
            	collapsed: false
            }/*,{
				xtype: 'add_criteria',
				title: 'Landcover'
			},{
				xtype: 'add_criteria',
				title: 'Slope'
			},{
				xtype: 'add_criteria',
				title: 'Distance to Stream'
			},{
				xtype: 'add_criteria',
				title: 'Watershed'
			},{
				xtype: 'add_criteria',
				title: 'Land Capability Class'
			},{
				xtype: 'add_criteria',
				title: 'Land Capability Subclass'
			},{
				xtype: 'add_criteria',
				title: 'Distance to Public Land'
			},{
				xtype: 'add_criteria',
				title: 'Density of Dairies'
			},{
				xtype: 'add_criteria',
				title: 'Subset of Land'
			}*/]
        });

        me.callParent(arguments);
        
		for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		var layer = DSS_globalQueryableLayers[i];
    		if (layer.isHidden()) {
    			me.add({xtype: 'add_criteria', title: layer.title, DSS_AssociatedLayer: layer, 
    				DSS_Description: layer.DSS_Description});
    		}
    	}
    }

});

