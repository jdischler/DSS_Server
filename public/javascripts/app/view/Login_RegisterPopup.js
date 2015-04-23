
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Login_RegisterPopup', {
    extend: 'Ext.window.Window',

    height: 210,
    width: 350,
    title: 'Registration',
	icon: 'app/images/tool.png',
    
    layout: 'absolute',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    maximizable: false,
    resizable: false,
    modal: true,

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
            	itemId: 'DSS_RegisterUser',  
				xtype: 'textfield',
				x: 10,
				y: 15,
				width: 300,
				fieldLabel: 'User Email',
				labelWidth: 110,
				labelAlign: 'right',
				emptyText: 'user',
				vtype: 'email',
				value: me.DSS_user, // value from login popup if they typed one
				validateBlank: true,
				allowBlank: false
			},{
            	itemId: 'DSS_Organization',  
				xtype: 'textfield',
				x: 10,
				y: 45,
				width: 300,
				fieldLabel: 'Organization',
				labelWidth: 110,
				labelAlign: 'right',
				emptyText: 'organization',
				allowBlank: true
			},{
            	itemId: 'DSS_RegisterPWD1',  
				xtype: 'textfield',
				x: 10,
				y: 75,
				width: 300,
				fieldLabel: 'Password',
				labelWidth: 110,
				labelAlign: 'right',
				inputType: 'password',
				emptyText: 'password',
				allowBlank: false
			},{
            	itemId: 'DSS_RegisterPWD2',  
				xtype: 'textfield',
				x: 10,
				y: 105,
				width: 300,
				fieldLabel: 'Re-Enter Password',
				labelWidth: 110,
				labelAlign: 'right',
				inputType: 'password',
				emptyText: 'password',
				allowBlank: false,
				validator: function(val) {
					var otherPwd = me.getComponent('DSS_RegisterPWD1').getValue();
					return (otherPwd == val);
				}
			},{
				xtype: 'button',
				x: 140, 
				y: 140,
				width: 80,
				text: 'Register',
				handler: function() {
					me.tryRegister();
				}
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    tryRegister: function() {
    
    	var me = this;
		var user = me.getComponent('DSS_RegisterUser').getValue();
		var organization = me.getComponent('DSS_Organization').getValue();
		var pwd1 = me.getComponent('DSS_RegisterPWD1').getValue();
		var pwd2 = me.getComponent('DSS_RegisterPWD1').getValue();
    	
		// TODO: better email validation...for now, length of 6 because 'a@b.co' is the shortest legal email address
		if (!user || user.length < 6) {
			Ext.MessageBox.alert('Register Error', 'The user field cannot be left empty and must be a valid email address!');
			return;
		}
		else if (!pwd1 || pwd1.length < 1 || !pwd2 || pwd2.length < 1) {
			Ext.MessageBox.alert('Register Error', 'The password field cannot be left empty!');
			return;
		}
		else if (pwd1 != pwd2) {
			Ext.MessageBox.alert('Register Error', 'The password fields do not match!');
			return;
		}
		
		var requestData = {
			user: user,
			organization: organization,
			pwd: pwd1
		};
		me.setDisabled(true);
		
		var obj = Ext.Ajax.request({
			url: location.href + 'register',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				me.setDisabled(false);				
			},
			
			failure: function(response, opts) {
				me.setDisabled(false);				
				Ext.MessageBox.alert('Registration Error', response.responseText);
			}
		});
	}
    
});


