# AndroidPermissions Library 
This is for making permission requesting easy.

`com.mr.permission.enums.PermissionCategory` is having most frequenlty used permissions to categorize the permissions.

`com.mr.permission.model.PermissionStringRes` is used to supply strings for displaying rational prompt.

`com.mr.permission.PermissionHelper` is used to check whether the app has supplied category permission or not.

`com.mr.permission.PermissionExt.kt` file contains fragment extension function for checking the permission and aslo requesting the permission.
Sample code for using this extension 

            val permissionCategories = listOf(PermissionCategory.PHOTOS)
            val permissionRes = PermissionStringRes(
                R.string.photos_read_rational_title,
                R.string.photos_read_rational_desc,
                R.string.photos_read_goto_settings_desc,
            )
            proceedWithPermissionCheck(permissionCategories, permissionRes) { granted, deniedList ->
                // Do your work here   
            }
          
