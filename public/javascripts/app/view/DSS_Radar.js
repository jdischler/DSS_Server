
// All this to add the option on the chart series: startDegrees
// which is just an offset from the normal starting position. Eg. -90 will start the chart
//	items at 12 o'clock position....

Ext.define('MyApp.view.DSS_Radar', {

    extend: 'Ext.chart.series.Radar',

    type: "dss_radar",
    alias: 'series.dss_radar',

    rad: Math.PI / 180,

    showInLegend: false,

    drawSeries: function() {
        var me = this,
            store = me.chart.getChartStore(),
            data = store.data.items,
            d, record,
            group = me.group,
            chart = me.chart,
            seriesItems = chart.series.items,
            s, sLen, series,
            field = me.field || me.yField,
            surface = chart.surface,
            chartBBox = chart.chartBBox,
            colorArrayStyle = me.colorArrayStyle,
            centerX, centerY,
            items,
            radius,
            maxValue = 0,
            fields = [],
            max = Math.max,
            cos = Math.cos,
            sin = Math.sin,
            pi2 = Math.PI * 2,
            l = store.getCount(),
            startPath, path, x, y, rho,
            i, nfields,
            seriesStyle = me.seriesStyle,
            axis = chart.axes && chart.axes.get(0),
            aggregate = !(axis && axis.maximum);

        me.setBBox();
        
        maxValue = aggregate? 0 : (axis.maximum || 0);
        
        Ext.apply(seriesStyle, me.style || {});

        //if the store is empty then there's nothing to draw
        if (!store || !store.getCount() || me.seriesIsHidden) {
            me.hide();
            me.items = [];
            if (me.radar) {
                me.radar.hide(true);
            }
            me.radar = null;
            return;
        }
        
        if(!seriesStyle['stroke']){
            seriesStyle['stroke'] = colorArrayStyle[me.themeIdx % colorArrayStyle.length];
        }

        me.unHighlightItem();
        me.cleanHighlights();

        centerX = me.centerX = chartBBox.x + (chartBBox.width / 2);
        centerY = me.centerY = chartBBox.y + (chartBBox.height / 2);
        me.radius = radius = Math.min(chartBBox.width, chartBBox.height) /2;
        me.items = items = [];

        if (aggregate) {
            //get all renderer fields
            for (s = 0, sLen = seriesItems.length; s < sLen; s++) {
                series = seriesItems[s];
                fields.push(series.yField);
            }
            //get maxValue to interpolate
            for (d = 0; d < l; d++) {
                record = data[d];
                for (i = 0, nfields = fields.length; i < nfields; i++) {
                    maxValue = max(+record.get(fields[i]), maxValue);
                }
            }
        }
        //ensure non-zero value.
        maxValue = maxValue || 1;
        //create path and items
        startPath = []; path = [];
        var startOffset = (me.startDegrees || 0) * (Math.PI / 180.0); 
        
        for (i = 0; i < l; i++) {
            record = data[i];
            rho = radius * record.get(field) / maxValue;
            x = rho * cos(i / l * pi2 + startOffset);
            y = rho * sin(i / l * pi2 + startOffset);
            if (i == 0) {
                path.push('M', x + centerX, y + centerY);
                startPath.push('M', 0.01 * x + centerX, 0.01 * y + centerY);
            } else {
                path.push('L', x + centerX, y + centerY);
                startPath.push('L', 0.01 * x + centerX, 0.01 * y + centerY);
            }
            items.push({
                sprite: false, //TODO(nico): add markers
                point: [centerX + x, centerY + y],
                storeItem: record,
                series: me
            });
        }
        path.push('Z');
        //create path sprite
        if (!me.radar) {
            me.radar = surface.add(Ext.apply({
                type: 'path',
                group: group,
                path: startPath
            }, seriesStyle || {}));
        }
        //reset on resizing
        if (chart.resizing) {
            me.radar.setAttributes({
                path: startPath
            }, true);
        }
        //render/animate
        if (chart.animate) {
            me.onAnimate(me.radar, {
                to: Ext.apply({
                    path: path
                }, seriesStyle || {})
            });
        } else {
            me.radar.setAttributes(Ext.apply({
                path: path
            }, seriesStyle || {}), true);
        }
        //render markers, labels and callouts
        if (me.showMarkers) {
            me.drawMarkers();
        }
        me.renderLabels();
        me.renderCallouts();
    },

    // @private callback for when placing a label sprite.
    onPlaceLabel: function(label, storeItem, item, i, display, animate, index) {
        var me = this,
            chart = me.chart,
            resizing = chart.resizing,
            config = me.label,
            format = config.renderer,
            field = config.field,
            centerX = me.centerX,
            centerY = me.centerY,
            opt = {
                x: Number(item.point[0]),
                y: Number(item.point[1])
            },
            x = opt.x - centerX,
            y = opt.y - centerY,
            theta = Math.atan2(y, x || 1),
            deg = theta * 180 / Math.PI,
            labelBox, direction;

            function fixAngle(a) {
                if (a < 0) {
                    a += 360;
                }
                return a % 360;
            }

        label.setAttributes({
            text: format(storeItem.get(field), label, storeItem, item, i, display, animate, index),
            hidden: true
        },
        true);

        // Move the label by half its height or width depending on 
        // the angle so the label doesn't overlap the graph.
        labelBox = label.getBBox();
        deg = fixAngle(deg);
        if ((deg > 45 && deg < 135) || (deg > 225 && deg < 315)) {
            direction = (deg > 45 && deg < 135 ? 1 : -1);
            opt.y += direction * labelBox.height/2;
        } else {
            direction = (deg >= 135 && deg <= 225 ? -1 : 1);
            opt.x += direction * labelBox.width/2;
        }

        if (resizing) {
            label.setAttributes({
                x: centerX,
                y: centerY
            }, true);
        }

        if (animate) {
            label.show(true);
            me.onAnimate(label, {
                to: opt
            });
        } else {
            label.setAttributes(opt, true);
            label.show(true);
        }
    }

});
