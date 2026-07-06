package com.example.q5digitalmarketplace;

import android.net.Uri;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest; // Standard single-level import
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CreateListingFragment extends Fragment {

    private EditText etTitle, etPrice, etDescription;
    private Spinner spinnerCategory, spinnerCondition, spinnerItemType, spinnerFaculty;
    private TextView tvCharCounter;
    private ImageView ivImagePreview;
    private LinearLayout layoutPlaceholder;
    private DatabaseHelper dbHelper;

    private String selectedImageUriStr = "";

    // CLEANER ALIGNED SYNTAX: Uses simple top-level class contract type mapping
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUriStr = uri.toString();
                    ivImagePreview.setImageURI(uri);
                    ivImagePreview.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
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
        tvCharCounter = view.findViewById(R.id.tv_char_counter);
        ivImagePreview = view.findViewById(R.id.iv_image_preview);
        layoutPlaceholder = view.findViewById(R.id.layout_placeholder);

        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        spinnerItemType = view.findViewById(R.id.spinner_item_type);
        spinnerFaculty = view.findViewById(R.id.spinner_faculty);

        setupDropdowns();
        setupDescriptionCounter();

        tvCharCounter.setText(getString(R.string.char_counter_template, 0));

        // CLEANER ALIGNED CALL: Matches standard SDK design patterns
        view.findViewById(R.id.card_add_photos).setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        view.findViewById(R.id.btn_add_item).setOnClickListener(v -> saveListingToDatabase());

        return view;
    }

    private void setupDropdowns() {
        String[] categories = {"Select Category", "Electronics", "Books", "Clothes", "Bags", "Services"};
        String[] conditions = {"Select Condition", "Brand New", "Like New", "Used"};
        String[] itemTypes = {"Select Item Type", "Buy", "Rental"};
        String[] faculties = {"Select Faculty", "Faculty of Computer Science", "Faculty of Engineering", "Faculty of Business"};

        initSpinner(spinnerCategory, categories);
        initSpinner(spinnerCondition, conditions);
        initSpinner(spinnerItemType, itemTypes);
        initSpinner(spinnerFaculty, faculties);
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
                tvCharCounter.setText(getString(R.string.char_counter_template, s.length()));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void saveListingToDatabase() {
        String title = etTitle.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        String category = spinnerCategory.getSelectedItem().toString();
        String condition = spinnerCondition.getSelectedItem().toString();

        if (title.isEmpty() || priceStr.isEmpty() || description.isEmpty() || selectedImageUriStr.isEmpty() ||
                spinnerCategory.getSelectedItemPosition() == 0 ||
                spinnerCondition.getSelectedItemPosition() == 0 ||
                spinnerItemType.getSelectedItemPosition() == 0 ||
                spinnerFaculty.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), R.string.error_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        String finalPrice = "RM " + priceStr;

        Listing newListing = new Listing(title, finalPrice, category, condition, selectedImageUriStr, description);

        try {
            dbHelper.insertListing(newListing);
            Toast.makeText(getContext(), R.string.success_listing_added, Toast.LENGTH_SHORT).show();
            clearForm();
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.error_db_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etTitle.setText("");
        etPrice.setText("");
        etDescription.setText("");
        selectedImageUriStr = "";
        ivImagePreview.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.VISIBLE);
        spinnerCategory.setSelection(0);
        spinnerCondition.setSelection(0);
        spinnerItemType.setSelection(0);
        spinnerFaculty.setSelection(0);
        tvCharCounter.setText(getString(R.string.char_counter_template, 0));
    }
}