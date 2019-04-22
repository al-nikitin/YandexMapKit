package com.appshop162.yandexmapkit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class LongTapDialog extends DialogFragment {

    public interface LongTapListener {
        public void onAClick(DialogFragment dialog);
        public void onBClick(DialogFragment dialog);
        public void onCancel(DialogFragment dialog);
    }

    LongTapListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (LongTapListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement LongTapListener");
        }
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.long_tap_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {
                switch (index) {
                    case 0:
                        listener.onAClick(LongTapDialog.this);
                        break;
                    case 1:
                        listener.onBClick(LongTapDialog.this);
                        break;
                    case 2:
                        listener.onCancel(LongTapDialog.this);
                        break;
                    default:
                        Toast.makeText(getContext(), "?????", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        return builder.create();
    }
}
