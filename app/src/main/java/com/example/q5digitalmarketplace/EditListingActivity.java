package com.example.q5digitalmarketplace;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class EditListingActivity extends AppCompatActivity {

    private EditText etTitle, etPrice, etDescription;
    private Spinner spinnerCategory, spinnerCondition, spinnerFaculty;
    private RadioGroup radioGroupType;
    private RadioButton radioBuy, radioRent;
    private ImageView ivImagePreview;
    private LinearLayout layoutPlaceholder;
    private TextView tvCharCounter;

    private DatabaseHelper dbHelper;
    private int listingId;
    private String currentImagePath;

    // Dropdown configuration arrays
    private final String[] categories = {"Electronics", "Books", "Clothes", "Bags", "Services", "Stationery", "Furniture", "Sports"};
    private final String[] conditions = {"Brand New", "Like New", "Used"};
    private final String[] faculties = {"General", "Faculty of Computer Science", "Faculty of Engineering", "Faculty of Business"};

    // Image Pick Subsystem
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentImagePath = uri.toString();
                    ivImagePreview.setImageURI(uri);
                    showImagePreview(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_create_listing);

        dbHelper = new DatabaseHelper(this);
        listingId = getIntent().getIntExtra("LISTING_ID", -1);

        // Bind Views matching your exact XML layout
        MaterialCardView cardAddPhotos = findViewById(R.id.card_add_photos);
        ivImagePreview = findViewById(R.id.iv_image_preview);
        layoutPlaceholder = findViewById(R.id.layout_placeholder);

        etTitle = findViewById(R.id.et_item_title);
        etPrice = findViewById(R.id.et_item_price);
        etDescription = findViewById(R.id.et_item_description);
        tvCharCounter = findViewById(R.id.tv_char_counter);

        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerCondition = findViewById(R.id.spinner_condition);
        spinnerFaculty = findViewById(R.id.spinner_faculty);

        radioGroupType = findViewById(R.id.radio_group_type);
        radioBuy = findViewById(R.id.radio_buy);
        radioRent = findViewById(R.id.radio_rent);

        MaterialButton btnSave = findViewById(R.id.btn_add_item);

        // Update button text using resource safely
        if (btnSave != null) {
            btnSave.setText(R.string.save_changes);
        }

        // Initialize drop selectors
        setupSpinner(spinnerCategory, categories);
        setupSpinner(spinnerCondition, conditions);
        setupSpinner(spinnerFaculty, faculties);

        // Character counter logic matching creation mechanics
        setupCharCounter();

        // Load existing product record data
        loadListingData();

        // Register action nodes
        if (cardAddPhotos != null) {
            cardAddPhotos.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveListingChanges());
        }
    }

    private void setupSpinner(Spinner spinner, String[] data) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupCharCounter() {
        if (etDescription == null || tvCharCounter == null) return;
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCounter.setText(s.length() + "/300");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showImagePreview(boolean hasImage) {
        if (ivImagePreview == null || layoutPlaceholder == null) return;
        if (hasImage) {
            ivImagePreview.setVisibility(View.VISIBLE);
            layoutPlaceholder.setVisibility(View.GONE);
        } else {
            ivImagePreview.setVisibility(View.GONE);
            layoutPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void loadListingData() {
        Listing listing = dbHelper.getListingById(listingId);
        if (listing != null) {
            if (etTitle != null) etTitle.setText(listing.getTitle());
            if (etPrice != null) etPrice.setText(listing.getPrice());
            if (etDescription != null) etDescription.setText(listing.getDescription());

            // Image visibility logic mapping
            currentImagePath = listing.getImagePath();
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                ivImagePreview.setImageURI(Uri.parse(currentImagePath));
                showImagePreview(true);
            } else {
                showImagePreview(false);
            }

            // Map Dropdown index targets
            setSpinnerSelection(spinnerCategory, categories, listing.getCategory());
            setSpinnerSelection(spinnerCondition, conditions, listing.getCondition());
            setSpinnerSelection(spinnerFaculty, faculties, listing.getFaculty());

            // Map Radio group status target values
            String typeValue = listing.getType();
            if (typeValue != null) {
                if (typeValue.equalsIgnoreCase("Buy") && radioBuy != null) {
                    radioBuy.setChecked(true);
                } else if (typeValue.equalsIgnoreCase("Rent") && radioRent != null) {
                    radioRent.setChecked(true);
                }
            }
        } else {
            Toast.makeText(this, "Listing items not located.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setSpinnerSelection(Spinner spinner, String[] array, String value) {
        if (spinner == null || value == null) return;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveListingChanges() {
        String updatedTitle = etTitle != null ? etTitle.getText().toString().trim() : "";
        String updatedPrice = etPrice != null ? etPrice.getText().toString().trim() : "";
        String updatedDescription = etDescription != null ? etDescription.getText().toString().trim() : "";

        String updatedCategory = spinnerCategory != null ? spinnerCategory.getSelectedItem().toString() : "";
        String updatedCondition = spinnerCondition != null ? spinnerCondition.getSelectedItem().toString() : "";
        String updatedFaculty = spinnerFaculty != null ? spinnerFaculty.getSelectedItem().toString() : "";

        // Handle Radio selection extraction strings
        String updatedType = "Buy";
        if (radioGroupType != null) {
            int selectedRadioId = radioGroupType.getCheckedRadioButtonId();
            if (selectedRadioId == R.id.radio_rent) {
                updatedType = "Rent";
            }
        }

        if (updatedTitle.isEmpty() || updatedPrice.isEmpty()) {
            Toast.makeText(this, "Required tracking elements cannot be left empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // CAROUSELL LOGIC ENGAGED: Check for modifications and trigger alerts BEFORE updating the DB row values
        dbHelper.checkAndNotifyPriceChange(this, listingId, updatedPrice, updatedTitle);

        // Commit updates down to SQLite
        boolean success = dbHelper.updateListing(listingId, updatedTitle, updatedPrice,
                updatedCategory, updatedCondition, currentImagePath, updatedDescription, updatedFaculty, updatedType);

        if (success) {
            Toast.makeText(this, "Listing edited and updated completely!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "SQL Update routine structural engine failure.", Toast.LENGTH_SHORT).show();
        }
    }
}