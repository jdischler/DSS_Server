
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
		yField: 'Baseline',
		startDegrees: -90,
		showInLegend: true,
		showMarkers: true,
		markerConfig: {
			radius: 2,
			size: 2,
			opacity: 0.75
		},
		tips: {
			//trackMouse: true,
			anchor: 'left',
			anchorOffset: 11,
			width: 180,
			height: 20,
			renderer: function(store, item) {
				this.setTitle(store.get('Bin') + ': ' + Ext.util.Format.number(store.get('Baseline'), '0.0#'));
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
//			trackMouse: true,
			anchor: 'left',
			anchorOffset: -11,
			width: 180,
			height: 20,
			renderer: function(store, item) {
				this.setTitle(store.get('Bin') + ': ' + Ext.util.Format.number(store.get('Scenario'), '0.0#'));
			}
		},
		style: {
			'stroke-width': 1,
			'fill-opacity': 0.1,
			'stroke-opacity': 1
		}
	}]

});

