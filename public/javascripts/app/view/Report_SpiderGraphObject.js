
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderGraphObject', {
    extend: 'Ext.chart.Chart',
    alias: 'widget.report_spiderObject',

    requires: [
    	'MyApp.view.DSS_Radar', // series
    	'MyApp.view.DSS_Radial' // axes
    ],
    
	height: 400,
	width: 500,
	insetPadding: 40,	
	animate: true,
				
	legend: {
		position: 'float',
		boxFill: '#fafcff',
		x: -38,
		y: -38
	},
	axes: [{
		title: '',
		type: 'DSS_Radial',
		position: 'dss_radial',
		startDegrees: -90,
		maximum: 100,
		fields: ['Bin']
	}],
	series: [{
		type: 'dss_radar',
		xField: 'Bin',
		yField: 'Current',
		startDegrees: -90,
		showInLegend: true,
		showMarkers: true,
		markerConfig: {
			radius: 2,
			size: 2,
			opacity: 0.75
		},
		tips: {
			trackMouse: true,
			width: 120,
			height: 50,
			renderer: function(store, item) {
				this.setTitle(store.get('Bin') + ': ' + store.get('Current'));
			}
		},
		style: {
			'stroke-width': 2,
			'fill-opacity': 0.1,
			'stroke-opacity': 1
		}
	},
	{
		type: 'dss_radar',
		xField: 'Bin',
		yField: 'Scenario',
		startDegrees: -90,
		showInLegend: true,
		showMarkers: true,
		markerConfig: {
			radius: 2,
			size: 2,
			opacity: 0.75
		},
		tips: {
			trackMouse: true,
			width: 150,
			height: 40,
			renderer: function(store, item) {
				this.setTitle(store.get('Bin') + ': ' + store.get('Scenario'));
			}
		},
		style: {
			'stroke-width': 2,
			'fill-opacity': 0.1,
			'stroke-opacity': 1
		}
	}]

});

