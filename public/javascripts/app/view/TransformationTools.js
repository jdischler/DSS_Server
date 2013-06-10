/*
 * File: app/view/TransformationTools.js
 */

Ext.define('MyApp.view.TransformationTools', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.transformationtools',

    height: 200,
    minHeight: 200,
    width: 300,
    collapsed: true,
    title: 'Transform Landscape',
    
    bodyStyle: {'background-color': '#fafcff'},
    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	},
    	icon: 'app/images/layers_icon.png'
    },
    
    activeTab: 1,
    
	tools:[{
		type: 'help',
		qtip: 'Transformation Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],

	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			el = c.header.insert(0,spc);
/*			// Test of adding a marker arrow...seems to add more clutter than anything
			var arrow = Ext.create('Ext.Img', {
				src: 'app/images/angled_arrow_icon.png',
				renderTo: Ext.getBody(),
				width: 16,
				height: 16
			});
			
			el = c.header.insert(0, arrow);
*/			
		}
	},
	
	//--------------------------------------------------------------------------    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'container',
				disabled: true
            },
            {
				xtype: 'panel',
				title: 'New Land Cover',
				layout: {
					type: 'absolute'
				},
				items: [{
					xtype: 'checkboxgroup',
					width: 400,
					x: 30,
					y: 10,
					columns: 1,
					vertical: true,
					items: [{
						xtype: 'checkboxfield',
						boxLabel: 'Corn / Soy'
					},
					{
						xtype: 'checkboxfield',
						boxLabel: 'Switchgrass'
					},
					{
						xtype: 'checkboxfield',
						boxLabel: 'Miscanthus'
					}]
				}],
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [{
						xtype: 'tbspacer', 
						width: 20
					},{
						xtype: 'button',
						icon: 'app/images/apply_icon.png',
						scale: 'medium',
						text: 'Apply'
					},
					{
						xtype: 'button',
						icon: 'app/images/revert_icon.png',
						scale: 'medium',
						text: 'Revert'
					},
					{
						xtype: 'button',
						icon: 'app/images/save_icon.png',
						scale: 'medium',
						text: 'Save'
					}]
				}]
			},
			{
				xtype: 'panel',
				title: 'Management Options'
			}]
        });

        me.callParent(arguments);
    }

});