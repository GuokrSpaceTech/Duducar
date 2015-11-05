package com.gc.materialdesign.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gc.materialdesign.R;
import com.gc.materialdesign.views.Button;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;

public class Dialog extends android.app.Dialog{

	Context context;
	View view;
	View backView;
	String message;
	TextView messageTextView;

	ButtonRectangle buttonAccept;
	ButtonRectangle buttonCancel;

	String buttonCancelText;

	View.OnClickListener onAcceptButtonClickListener;
	View.OnClickListener onCancelButtonClickListener;


	public Dialog(Context context,String message) {
		super(context, android.R.style.Theme_Translucent);
		this.context = context;// init Context
		this.message = message;
	}

	public void addCancelButton(String buttonCancelText){
		this.buttonCancelText = buttonCancelText;
	}

	public void addCancelButton(String buttonCancelText, View.OnClickListener onCancelButtonClickListener){
		this.buttonCancelText = buttonCancelText;
		this.onCancelButtonClickListener = onCancelButtonClickListener;
	}


	@Override
	  protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.dialog);

		view = (RelativeLayout)findViewById(R.id.contentDialog);
		backView = (RelativeLayout)findViewById(R.id.dialog_rootView);
		backView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getX() < view.getLeft()
						|| event.getX() >view.getRight()
						|| event.getY() > view.getBottom()
						|| event.getY() < view.getTop()) {
					dismiss();
				}
				return false;
			}
		});


	    this.messageTextView = (TextView) findViewById(R.id.message);
	    setMessage(message);

	    this.buttonAccept = (ButtonRectangle) findViewById(R.id.button_accept);
	    buttonAccept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if (onAcceptButtonClickListener != null)
					onAcceptButtonClickListener.onClick(v);
			}
		});

        this.buttonCancel = (ButtonRectangle) findViewById(R.id.button_cancel);
//        this.buttonCancel.setVisibility(View.VISIBLE);
        buttonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (onCancelButtonClickListener != null)
                    onCancelButtonClickListener.onClick(v);
            }
        });

    }

	@Override
	public void show() {
		// TODO 自动生成的方法存根
		super.show();
		// set dialog enter animations
		view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_main_show_amination));
		backView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_root_show_amin));
	}

	// GETERS & SETTERS

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
		messageTextView.setText(message);
	}

	public TextView getMessageTextView() {
		return messageTextView;
	}

	public void setMessageTextView(TextView messageTextView) {
		this.messageTextView = messageTextView;
	}


	public ButtonRectangle getButtonAccept() {
		return buttonAccept;
	}

	public void setButtonAccept(ButtonRectangle buttonAccept) {
		this.buttonAccept = buttonAccept;
	}

	public ButtonRectangle getButtonCancel() {
		return buttonCancel;
	}

	public void setButtonCancel(ButtonRectangle buttonCancel) {
		this.buttonCancel = buttonCancel;
	}

	public void setOnAcceptButtonClickListener(
			View.OnClickListener onAcceptButtonClickListener) {
		this.onAcceptButtonClickListener = onAcceptButtonClickListener;
		if(buttonAccept != null)
			buttonAccept.setOnClickListener(onAcceptButtonClickListener);
	}

	public void setOnCancelButtonClickListener(
			View.OnClickListener onCancelButtonClickListener) {
		this.onCancelButtonClickListener = onCancelButtonClickListener;
		if(buttonCancel != null)
			buttonCancel.setOnClickListener(onCancelButtonClickListener);
	}

	@Override
	public void dismiss() {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_main_hide_amination);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.post(new Runnable() {
					@Override
					public void run() {
			        	Dialog.super.dismiss();
			        }
			    });

			}
		});
		Animation backAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_root_hide_amin);

		view.startAnimation(anim);
		backView.startAnimation(backAnim);
	}



}
