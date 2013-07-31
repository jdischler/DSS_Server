/*
 * File: app/view/ScenarioTools.js
 */

//------------------------------------------------------------------------------
var ScenarioGridStore = Ext.create('Ext.data.Store', {
		
    fields: ['Active', 'SelectionName', 'TransformText', 'ManagementText', 'Transform', 'Query'],
    data: {
        items: [{ 
        	Active: true, 
            SelectionName: 'Untitled Selection', 
        	TransformText: 'To Woodland',
        	ManagementText: '<b><i>Management Options:</i></b></br>None',
        	Transform: 11,
        	Query: null
        }/*, {
        	Active: false, 
            SelectionName: 'Some other', 
        	TransformText: 'some disabled thing',
        	Transform: null,
        	Query: null
        }*/]
    },
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    }
});
 
// Scenario Summary....
//------------------------------------------------------------------------------
Ext.define('MyApp.view.ScenarioTools', {
		
    extend: 'Ext.grid.Panel',
    alias: 'widget.scenariotools',

    requires: [
    	'MyApp.view.GlobalScenarioPopup',
    	'MyApp.view.TransformPopup'
    ],
    
    id: 'DSS_ScenarioSummary',
    height: 180,
    minHeight: 180,
    maxHeight: 180,
    width: 300,
	dock: 'bottom',
    
//    header: true,
    title: 'Scenario Management',
	viewConfig: {
		stripeRows: true
	},
    store: ScenarioGridStore,
    
    enableColumnHide: false,
    enableColumnMove: false,
    sortableColumns: false,
    
    bodyStyle: {'background-color': '#fafcff'},
	icon: 'app/images/magnify_icon.png',
    
	dockedItems: [{
		xtype: 'toolbar',
		dock: 'bottom',
		items: [{
			xtype: 'button',
			icon: 'app/images/new_icon.png',
			scale: 'medium',
			text: 'New',
			disabled: true
		},
		{
			xtype: 'button',
			icon: 'app/images/save_icon.png',
			scale: 'medium',
			text: 'Save',
			disabled: true
		},
		{
			xtype: 'button',
			icon: 'app/images/load_icon.png',
			scale: 'medium',
			text: 'Load',
			disabled: true
		},
		{
			xtype: 'button',
			icon: 'app/images/globe_icon.png',
			scale: 'medium',
			text: 'Global Assumptions',
			tooltip: {
				text: 'Specify any global assumptions for this scenario',
				showDelay: 100,
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				var window = Ext.create('MyApp.view.GlobalScenarioPopup');
				var pos = [self.getPosition()[0], self.up().getPosition()[1]];
//				console.log(pos);
				window.show();
				var size = window.getSize();
//				console.log(size);
				// eh, just move it down some relative to the clicked button...
				window.showAt(pos[0],// + self.width,
									(pos[1]) - size.height,
									false);
			}
		},
		{
			xtype: 'button',
			id: 'DSS_runModelButton', // must be unique
			icon: 'app/images/go_icon.png',
			scale: 'medium',
			text: 'Run',
			tooltip: {
				text: 'Run the Model using the current scenario',
				showDelay: 100,
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				this.up().up().buildModel();
			}
		}]
	}],
	
	plugins: [
		Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 2,
			listeners: {
				edit: {
					fn: function(editor, e) {
						// no real need for validation, but if we don't commit the changes,
						//	changed fields will show a red triangle in the corner...
						e.record.commit();
					}
				}
			}
		})
	],
	viewConfig: {
		getRowClass: function(record, index) {
			var c = record.get('Active')
			if (c == false) {
				return 'dss-greyed';
			}
		}
	},
	
	listeners: {
		celldblclick: function(me, td, cellIndex, record, tr, rowIndex, e, eOpts) {
			
			if (cellIndex != 1) return;
			me.up().showTransformPopup(me, rowIndex);
		}
	},
	//--------------------------------------------------------------------------
	columns: {
		items:[{
			dataIndex: 'SelectionName',
			text: 'Selection',
			width: 115,
			resizable: false,
			editor: {
				xtype: 'textfield',
				allowBlank: false
			},
			tdCls: 'dss-grey-scenario-grid'
		},
		{
			dataIndex: 'TransformText',
			text: 'Transforms To & Managment Options',
			width: 220,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid',
			renderer: function(value, meta, record) {
				meta.tdAttr = 'data-qtip="' + record.get("ManagementText") + '"';
				return value;
			}
		},
		{
			xtype: 'actioncolumn',
			width: 20,
			resizable: false,
			icon: 'app/images/switch_icon.png',
			tooltip: 'Edit Transform & Management Options',
			handler: function(grid, rowIndex, colIndex) {
				grid.up().showTransformPopup(grid, rowIndex);
			}
		},
		{
			dataIndex: 'Active',
			text: 'Active',
		//	xtype: 'checkcolumn',
			width: 43,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid'
		}]
	},

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    },

	//--------------------------------------------------------------------------
	showTransformPopup: function(grid,rowIndex) {
		
		var record = grid.getStore().getAt(rowIndex);
		var transform = record.get('Transform');
		var window = Ext.create('MyApp.view.TransformPopup', {
			DSS_Transform: {Type: transform},
			listeners: {
				beforedestroy: {
					fn: function() {
						if (window.DSS_Transform) {
							record.set('Transform', window.DSS_Transform.Type);
							record.set('TransformText', window.DSS_Transform.Text);
							record.set('ManagementText', window.DSS_Transform.Management);
							record.commit();
						}
					}
				}
			}});
		var pos = grid.getPosition(true);
		console.log(pos);
		window.show();
		// eh, just move it down some relative to the rowIndex clicked...
		window.setPosition(pos[0] + grid.width,
							(pos[1] - window.getSize().height),
							false);
	},
	
	//--------------------------------------------------------------------------
	buildModel: function() {
	
		var requestData = {
			clientID: 12345, //temp
			transforms: []
		};
		
		var landUse = this.getStore().getAt(0).data.Transform;
		if (landUse == null) {
			landUse = 1; // blurf, set to corn....
		}
		
		var transform = {
			queryLayers: [],
			newLandUse: landUse
		};
		
		var haveQuery = false;
		for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
			
			if (DSS_globalQueryableLayers[i].includeInQuery()) {
				var queryComp = DSS_globalQueryableLayers[i].getSelectionCriteria();
				transform.queryLayers.push(queryComp);
				haveQuery = true;
			}
		}
	
		requestData.transforms.push(transform);
		
		console.log(requestData);
		if (haveQuery) {
			this.submitModel(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
	},
	
    //--------------------------------------------------------------------------
    submitModel: function(queryJson) {
    	
		var button = Ext.getCmp('DSS_runModelButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);

		var obj = Ext.Ajax.request({
			url: location.href + 'models',
			jsonData: queryJson,
			timeout: 5 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				var obj = JSON.parse(response.responseText);
				console.log("success: ");
				console.log(obj);
				
				Ext.getCmp('Model_Graph').SetData(obj);
				button.setIcon('app/images/go_icon.png');
				button.setDisabled(false);
				
				var reportPanel = Ext.getCmp('DSS_report_panel');
				if (reportPanel.getCollapsed() != false) {
					reportPanel.expand();
				}
			},
			
			failure: function(respose, opts) {
				button.setIcon('app/images/go_icon.png');
				button.setDisabled(false);
				alert("Model run failed, request timed out?");
			}
		});
	}

});

