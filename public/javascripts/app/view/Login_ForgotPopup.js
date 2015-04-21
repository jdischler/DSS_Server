
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Login_ForgotPopup', {
    extend: 'Ext.window.Window',

    height: 120,
    width: 350,
    title: 'Reset Password',
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
            	itemId: 'DSS_RegisteredUser',  
				xtype: 'textfield',
				x: 10,
				y: 15,
				width: 300,
				fieldLabel: 'Registered Email',
				labelWidth: 110,
				labelAlign: 'right',
				emptyText: 'user',
				vtype: 'email',
				validateBlank: true,
				allowBlank: false
			},{
            	itemId: 'DSS_VerificationCode',  
				xtype: 'textfield',
				x: 10,
				y: 45,
				width: 300,
				fieldLabel: 'Verification Code',
				labelWidth: 110,
				labelAlign: 'right',
				emptyText: 'emailed code',
				allowBlank: true,
				hidden: true
			},{
            	itemId: 'DSS_NewPWD1',  
				xtype: 'textfield',
				x: 10,
				y: 75,
				width: 300,
				fieldLabel: 'New Password',
				labelWidth: 110,
				labelAlign: 'right',
				inputType: 'password',
				emptyText: 'password',
				allowBlank: false,
				hidden: true
			},{
            	itemId: 'DSS_NewPWD2',  
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
				hidden: true,
				validator: function(val) {
					var otherPwd = me.getComponent('DSS_NewPWD1').getValue();
					return (otherPwd == val);
				}
			},{
				itemId: 'DSS_ForgotPasswordButton',
				xtype: 'button',
				x: 130, 
				y: 60,
				width: 100,
				text: 'Request Reset',
				handler: me.requestReset
			}]
        });

        me.callParent(arguments);
    },

	//--------------------------------------------------------------------------
    requestReset: function() {
    
    	var me = this.up(); //scope is button so go up one level
		var user = me.getComponent('DSS_RegisteredUser').getValue();
    	
		// TODO: better email validation...for now, length of 6 because 'a@b.co' is the shortest legal email address
		if (!user || user.length < 6) {
			Ext.MessageBox.alert('Register Error', 'The register user email field cannot be left empty and must be a valid email address!');
			return;
		}
		
		var requestData = {
			user: user
		};
		
		var obj = Ext.Ajax.request({
			url: location.href + 'requestReset',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				var button = me.getComponent('DSS_ForgotPasswordButton');
				me.setHeight(210);
				me.getComponent('DSS_RegisteredUser').setDisabled(true);
				me.getComponent('DSS_VerificationCode').show();
				me.getComponent('DSS_NewPWD1').show();
				me.getComponent('DSS_NewPWD2').show();
				button.setText('Try Reset');
				button.setLocalY(140);
				button.handler = me.tryReset;
				
				Ext.MessageBox.alert('Password Reset', 'A reset code has been emailed to you. Please copy the verification code and paste it into the next form to complete your password reset.'); 
			},
			
			failure: function(response, opts) {
				Ext.MessageBox.alert('Password Reset Error', response.responseText);
			}
		});
	},

    //--------------------------------------------------------------------------
    tryReset: function() {
    
    	var me = this.up(); //scope is button so go up one level
		var user = me.getComponent('DSS_RegisteredUser').getValue();
		var code = me.getComponent('DSS_VerificationCode').getValue();
		var pwd1 = me.getComponent('DSS_NewPWD1').getValue();
		var pwd2 = me.getComponent('DSS_NewPWD1').getValue();
    	
		// TODO: better email validation...for now, length of 6 because 'a@b.co' is the shortest legal email address
		if (!user || user.length < 6) {
			Ext.MessageBox.alert('Error', 'The user field cannot be left empty and must be a valid email address!');
			return;
		}
		else if (!pwd1 || pwd1.length < 1 || !pwd2 || pwd2.length < 1) {
			Ext.MessageBox.alert('Error', 'The password field cannot be left empty!');
			return;
		}
		else if (pwd1 != pwd2) {
			Ext.MessageBox.alert('Error', 'The password fields do not match!');
			return;
		}
		
		var tryResetData = {
			user: user,
			code: code,
			pwd: pwd1
		};
		
		var obj = Ext.Ajax.request({
			url: location.href + 'tryReset',
			jsonData: tryResetData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				Ext.MessageBox.alert('Password Reset Success', response.responseText);
			},
			
			failure: function(response, opts) {
				Ext.MessageBox.alert('Password Reset Error', response.responseText);
			}
		});
	}
    
});


