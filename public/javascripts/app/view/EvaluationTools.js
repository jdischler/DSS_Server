/*
 * File: app/view/EvaluationTools.js
 */

Ext.define('MyApp.view.EvaluationTools', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.evaluationtools',

    height: 300,
    width: 300,
    title: 'Simulation Results',
	icon: 'app/images/scenario_icon.png',
    activeTab: 0,

    tools:[{
		type: 'help',
		qtip: 'Simulation Results Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],
    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'panel',
				title: 'Outcomes',
				layout: {
					type: 'absolute'
				},
				items: [{
					xtype: 'chart',
					data: 'data',
					x: 0,
					y: 0,
					height: 120,
					width: 180,
					insetPadding: 30,
					animate: true,
					store: 'store1',
					axes: [{
						position: 'gauge',
						title: 'Sustainability',
						type: 'Gauge',
						margin: 8,
						maximum: 100,
						minimum: 0,
						steps: 4
					}],
					series: [{
						type: 'gauge',
						angleField: 'data1',
						donut: 60,
						needle: false,
						highlight: {
							fill: '#ad2',
							"stroke-width": 1,
							stroke: '#228'
						},
						tips: {
							trackMouse: true,
							width: 90,
							height: 26,
							renderer: function(storeItem, item) {
									this.setTitle('Value: ' + storeItem.get('data1'));
							}
						}
					}]
				},
				{
					xtype: 'chart',
					data: 'data',
					x: 200,
					y: 0,
					height: 120,
					width: 180,
					insetPadding: 30,
					animate: true,
					store: 'store1',
					axes: [{
						position: 'gauge',
						title: 'Emissions',
						type: 'Gauge',
						margin: 8,
						maximum: 100,
						minimum: 0,
						steps: 4
					}],
					series: [{
						type: 'gauge',
						angleField: 'data2',
						donut: 60,
						needle: false,
						highlight: {
							fill: '#ad2',
							"stroke-width": 1,
							stroke: '#228'
						},
						tips: {
							trackMouse: true,
							width: 90,
							height: 26,
							renderer: function(storeItem, item) {
									this.setTitle('Value: ' + storeItem.get('data2'));
							}
						}
					}]
				},
				{
					xtype: 'chart',
					data: 'data',
					x: 0,
					y: 120,
					height: 120,
					width: 180,
					insetPadding: 30,
					animate: true,
					store: 'store1',
					axes: [{
						position: 'gauge',
						title: 'Water Qual.',
						type: 'Gauge',
						margin: 8,
						maximum: 100,
						minimum: 0,
						steps: 4
					}],
					series: [{
						type: 'gauge',
						angleField: 'data3',
						donut: 60,
						needle: false,
						highlight: {
							fill: '#ad2',
							"stroke-width": 1,
							stroke: '#228'
						},
						tips: {
							trackMouse: true,
							width: 90,
							height: 26,
							renderer: function(storeItem, item) {
									this.setTitle('Value: ' + storeItem.get('data3'));
							}
						}
					}]
				},
				{
					xtype: 'chart',
					data: 'data',
					x: 200,
					y: 120,
					height: 120,
					width: 180,
					insetPadding: 30,
					animate: true,
					store: 'store1',
					axes: [{
						position: 'gauge',
						title: 'Biodiversity',
						type: 'Gauge',
						margin: 8,
						maximum: 100,
						minimum: 0,
						steps: 4
					}],
					series: [{
						type: 'gauge',
						angleField: 'data4',
						donut: 60,
						needle: false,
						highlight: {
							fill: '#ad2',
							"stroke-width": 1,
							stroke: '#228'
						},
						tips: {
							trackMouse: true,
							width: 90,
							height: 26,
							renderer: function(storeItem, item) {
									this.setTitle('Value: ' + storeItem.get('data4'));
							}
						}
					}]
				}]
			},
			{
				xtype: 'panel',
				title: 'Assumptions'
			}]
        });

        me.callParent(arguments);
    }

});