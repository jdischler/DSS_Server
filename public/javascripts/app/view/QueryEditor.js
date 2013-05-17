Ext.define('MyApp.view.QueryEditor', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.queryEditor',
    
    requires: [
    	'MyApp.view.QueryEditor_Node',
      'MyApp.view.QueryEditor_GIS'
    ],

	frame: false,
	layout: {
			type: 'absolute'
	},
	bodyStyle: 'background-color: #555;',

	title: 'Query Editor / Viewer',
//	titleAlign: 'center',
	collapsible: true,
//	collapsed: true,
	animCollapse: false,
	icon: 'app/images/query_icon.png',
	
	tools: [{
		type: 'gear',
		tooltip: 'Open Query Builder',
		handler: function(event, toolEl, panel) {
			var window = Ext.create(MyApp.view.QueryEditor_GIS);
			window.show();
		}
	}],

	height: 100,
	overflowX: 'auto',
	weight: 11,

    initComponent: function() {
        var me = this;

        this.nodeArray = new Array();
        this.DSS_textArray = ['Watershed','..River','.Prairie'],
        this.DSS_textArrayCtr = 0;
        
		Ext.applyIf(me, {
			items: [{
				itemId: 'draw',
				xtype: 'draw',
				viewbox: false,
				width: 600,
				height: 90,
				items: [{
					type: 'rect',
					fill: '#555',
					x: 0,
					y: 0,
					width: 600,
					height: 90
				},
				{
					type: 'circle',
					fill: '#8B4',
					radius: 10,
					x: 12,
					y: 12,
					listeners: {
						click: this.onClickAddNode,
						mouseover: this.onMouseOverAddNode,
						mouseout: this.onMouseOutAddNode,
						scope: this
					}
				},
				{
					type: 'circle',
					fill: '#B84',
					radius: 10,
					x: 34,
					y: 12
				}]
			}]
		});

        me.callParent(arguments);
    },
    
    placeNodes: function() {
    	var nodeStartX = 10;
    	var nodeY = 35;
    	for (var index = 0; index < this.nodeArray.length; index++) {
    		this.nodeArray[index].nodeSprite[0].setAttributes({
				translate: {
					x: nodeStartX,
					y: nodeY
				}
    		}, true);
    		this.nodeArray[index].nodeSprite[1].setAttributes({
    				translate: {
    					x: nodeStartX + 4,
    					y: nodeY + 14
    				}
    		}, true);
    		nodeStartX += 110;
    	}
    },
    
    onClickAddNode: function(sprite) {
    	var node = Ext.create('MyApp.view.QueryEditor_Node');
    	//var draw = this.getComponent('draw');
    	node.attach(sprite.surface,0,0,this.DSS_textArray[this.DSS_textArrayCtr % 3]);
    	this.DSS_textArrayCtr++;
    	this.nodeArray.push(node);
    	this.placeNodes();
    },
    
    onMouseOverAddNode: function(sprite) {
    	sprite.stopAnimation().animate({
    			duration: 150,
    			to: {
    				fill: '#a4ff5f',
    				scale: { 
    					x: 1,
    					y: 1.15
    				}
    			}
    	});
    },
    
    onMouseOutAddNode: function(sprite) {
    	sprite.stopAnimation().animate({
    			duration: 600,
    			to: {
    				fill: '#8B4',
    				scale: { 
    					x: 1,
    					y: 1
    				}
    			}
    	});
    }

});

