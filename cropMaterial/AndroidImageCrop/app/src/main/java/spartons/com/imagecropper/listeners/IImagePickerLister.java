package spartons.com.imagecropper.listeners;

import spartons.com.imagecropper.enums.ImagePickerEnum;

@FunctionalInterface
public interface IImagePickerLister {
    void onOptionSelected(ImagePickerEnum imagePickerEnum);
}
