package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CreateListingFragment extends Fragment {

    private EditText etTitle, etPrice, etDescription;
    private Spinner spinnerCategory, spinnerCondition, spinnerFaculty;
    private RadioGroup radioGroupType;
    private TextView tvCharCounter;
    private ImageView ivImagePreview;
    private LinearLayout layoutPlaceholder;
    private DatabaseHelper dbHelper;

    private String selectedImageUriStr = "";

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    requireContext().getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    selectedImageUriStr = uri.toString();
                    ivImagePreview.setImageURI(uri);
                    ivImagePreview.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_listing, container, false);

        dbHelper = new DatabaseHelper(getContext());

        etTitle = view.findViewById(R.id.et_item_title);
        etPrice = view.findViewById(R.id.et_item_price);
        etDescription = view.findViewById(R.id.et_item_description);
        radioGroupType = view.findViewById(R.id.radio_group_type);
        tvCharCounter = view.findViewById(R.id.tv_char_counter);
        ivImagePreview = view.findViewById(R.id.iv_image_preview);
        layoutPlaceholder = view.findViewById(R.id.layout_placeholder);

        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        spinnerFaculty = view.findViewById(R.id.spinner_faculty);

        setupDropdowns();
        setupDescriptionCounter();

        // 🛠️ FIXED: Removed the btn_back_create_listing search declaration and event binding block

        view.findViewById(R.id.card_add_photos).setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        view.findViewById(R.id.btn_add_item).setOnClickListener(v -> saveListingToDatabase());

        return view;
    }

    private void setupDropdowns() {
        initSpinner(spinnerCategory, new String[]{
                "Select Category",
                "Electronics",
                "Books",
                "Clothes",
                "Bags",
                "Services",
                "Stationery",
                "Furniture",
                "Sports"
        });
        initSpinner(spinnerCondition, new String[]{"Select Condition", "Brand New", "Like New", "Used"});
        initSpinner(spinnerFaculty, new String[]{"Select Faculty", "Faculty of Computer Science", "Faculty of Engineering", "Faculty of Business"});
    }

    private void initSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupDescriptionCounter() {
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvCharCounter != null) {
                    tvCharCounter.setText(getString(R.string.char_counter_template, s.length()));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void saveListingToDatabase() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        int sellerId = (userEmail != null) ? dbHelper.getStuIDByEmail(userEmail) : -1;

        if (sellerId == -1) {
            Toast.makeText(getContext(), "Session error: Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (radioGroupType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Please select Buy or Rent.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String title = etTitle.getText().toString().trim();
        final String priceInput = etPrice.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();
        final String category = spinnerCategory.getSelectedItem().toString();
        final String condition = spinnerCondition.getSelectedItem().toString();
        final String faculty = spinnerFaculty.getSelectedItem().toString();
        final String type = ((RadioButton) requireView().findViewById(radioGroupType.getCheckedRadioButtonId())).getText().toString();

        if (title.isEmpty() || priceInput.isEmpty() || description.isEmpty() || selectedImageUriStr.isEmpty() ||
                spinnerCategory.getSelectedItemPosition() == 0 ||
                spinnerCondition.getSelectedItemPosition() == 0 ||
                spinnerFaculty.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalPrice = priceInput.toUpperCase().startsWith("RM") ? priceInput : "RM " + priceInput;

        Listing newListing = new Listing(title, finalPrice, category, condition, selectedImageUriStr, description, faculty, type, sellerId);
        newListing.setStatus("Active");

        if (dbHelper.insertListing(newListing)) {
            Toast.makeText(getContext(), "Listing added successfully!", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(getContext(), "Database error.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etTitle.setText("");
        etPrice.setText("");
        etDescription.setText("");
        radioGroupType.clearCheck();
        selectedImageUriStr = "";
        ivImagePreview.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
        spinnerCategory.setSelection(0);
        spinnerCondition.setSelection(0);
        spinnerFaculty.setSelection(0);
        if (tvCharCounter != null) {
            tvCharCounter.setText(getString(R.string.char_counter_template, 0));
        }
    }
}