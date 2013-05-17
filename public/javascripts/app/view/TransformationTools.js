/*
 * File: app/view/TransformationTools.js
 */

Ext.define('MyApp.view.TransformationTools', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.transformationtools',

    height: 200,
    width: 300,
    title: 'Transform Landscape',
	icon: 'app/images/layers_icon.png',
    activeTab: 0,
    
	tools:[{
		type: 'help',
		qtip: 'Transformation Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'panel',
				bodyPadding: 10,
				title: 'New Land Cover',
				items: [{
					xtype: 'checkboxgroup',
					width: 400,
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