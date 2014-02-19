
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderGraphObject', {
    extend: 'Ext.chart.Chart',
    alias: 'widget.report_spiderObject',

	height: 400,
	width: 500,
	insetPadding: 40,	
	animate: true,
				
	legend: {
		position: 'float',
		boxFill: '#fafcff',
		x: -32,
		y: -30
	},
	axes: [{
		title: '',
		type: 'Radial',
		position: 'radial',
		maximum: 100,
		fields: ['Bin']
	}],
	series: [{
		type: 'radar',
		xField: 'Bin',
		yField: 'Default',
		showInLegend: true,
		showMarkers: true,
		markerConfig: {
			radius: 3,
			size: 3
		},
		tips: {
			trackMouse: true,
			width: 120,
			height: 50,
			renderer: function(store, item) {
				this.setTitle(store.get('Bin') + ': ' + store.get('Default'));
			}
		},
		style: {
			'stroke-width': 2,
			'fill-opacity': 0.1,
			'stroke-opacity': 1
		}
	},
	{
		type: 'radar',
		xField: 'Bin',
		yField: 'Transform',
		showInLegend: true,
		showMarkers: true,
		markerConfig: {
			radius: 3,
			size: 3
		},
		tips: {
			trackMouse: true,
			width: 150,
			height: 40,
			renderer: function(store, item) {
				this.setTitle(store.get('Bin') + ': ' + store.get('Transform'));
			}
		},
		style: {
			'stroke-width': 2,
			'fill-opacity': 0.1,
			'stroke-opacity': 1
		}
	}]

});

