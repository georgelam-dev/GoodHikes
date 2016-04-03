/*------------------------------------------------------------------------------
 *   Authors: Slavik, George, Thao, Chelsea
 *   Copyright: (c) 2016 Team Magic
 *
 *   This file is part of GoodHikes.
 *
 *   GoodHikes is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GoodHikes is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GoodHikes.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uwaterloo.magic.goodhikes;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class AddMilestoneDialogFragment extends DialogFragment {
    MapsActivity mapsActivity;
    private ImageView previewImage;
    protected static final String LOG_TAG = "AddMilestoneDialogFragment";
    private final int RESULT_LOAD_IMG = 2;
    private final int RESULT_CAPTURE_IMG = 3;

    public AddMilestoneDialogFragment(){
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 300, 225, false);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Couldn't load the image selected");
//                    e.printStackTrace();
                }
//                previewImage.setImageURI(selectedImage);
                previewImage.setImageBitmap(bitmap);
            }
        }
        if (requestCode == RESULT_CAPTURE_IMG) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 300, 225, false);
                previewImage.setImageBitmap(imageBitmap);
            }
        }
    }


    public static AddMilestoneDialogFragment newInstance() {
        AddMilestoneDialogFragment dialogFragment = new AddMilestoneDialogFragment();
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mapsActivity = (MapsActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_add_milestone, null);
        previewImage = (ImageView) dialog.findViewById(R.id.previewImage);

        if (savedInstanceState != null) {
            Bitmap image = savedInstanceState.getParcelable("image");
            if(image!=null){
                previewImage.setImageBitmap(image);
            }
        }

        builder.setView(dialog)
                .setTitle(R.string.add_milestone)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText noteField = (EditText) ((Dialog) dialog).findViewById(R.id.note);
                        String note = noteField.getText().toString();
                        Bitmap image = previewImage.getDrawable() == null ? null : ((BitmapDrawable) previewImage.getDrawable()).getBitmap();

                        mapsActivity.imageSelected(image, note);
                        Toast.makeText(getActivity(), "Milestone added", Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; milestone added");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; milestone cancelled");
                    }
                });

        Button addImageButton = (Button) dialog.findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });

        Button cameraButton = (Button) dialog.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to open device's Camera
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, RESULT_CAPTURE_IMG);
            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try{
            Bitmap bitmap = ((BitmapDrawable) previewImage.getDrawable()).getBitmap();
            outState.putParcelable("image", bitmap);
        }
        catch(NullPointerException e){
            Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; No image stored in ImageView");
        }
    }
}