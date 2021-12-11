package com.github.ghmxr.ftpshare.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.ftpshare.Constants;
import com.github.ghmxr.ftpshare.R;
import com.github.ghmxr.ftpshare.data.AccountItem;
import com.github.ghmxr.ftpshare.services.FtpService;
import com.github.ghmxr.ftpshare.utils.MySQLiteOpenHelper;

public abstract class AccountActivity extends BaseActivity {
    public AccountItem item;
    public TextView tv_account,tv_password,tv_path;
    public CheckBox cb_writable;
    String checkString;
    long first_clicked_back=0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){e.printStackTrace();}
        tv_account=findViewById(R.id.account_user_value);
        tv_password=findViewById(R.id.account_password_value);
        tv_path=findViewById(R.id.account_path_value);
        cb_writable=findViewById(R.id.account_writable_cb);
        initializeAccountItem();
        try{
            if(item==null) item=new AccountItem();
            checkString=item.toString();
           tv_account.setText(item.account);
           tv_password.setText(getPasswordDisplayValue(item.password));
           tv_path.setText(item.path);
           cb_writable.setChecked(item.writable);

           findViewById(R.id.account_user).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   View dialogView=LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_with_edittext,null);
                   final EditText editText=dialogView.findViewById(R.id.dialog_edittext);
                   editText.setText(item.account);
                   editText.setHint(getResources().getString(R.string.account_user_dialog_edittext_hint));
                   final AlertDialog dialog=new AlertDialog.Builder(AccountActivity.this)
                           .setTitle(getResources().getString(R.string.account_user_dialog_title))
                           .setView(dialogView)
                           .setPositiveButton(getResources().getString(R.string.dialog_button_confirm),null)
                           .setNegativeButton(getResources().getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {

                               }
                           })
                           .show();
                   dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           item.account=editText.getText().toString().trim();
                           dialog.cancel();
                           tv_account.setText(item.account);
                       }
                   });
               }
           });

           findViewById(R.id.account_password).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   View dialogView=LayoutInflater.from(AccountActivity.this).inflate(R.layout.layout_with_edittext,null);
                   final EditText editText=dialogView.findViewById(R.id.dialog_edittext);
                   editText.setText(item.password);
                   editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                   editText.setTag(true);
                   editText.setHint(getResources().getString(R.string.account_password_dialog_edittext_hint));
                   final AlertDialog dialog=new AlertDialog.Builder(AccountActivity.this)
                           .setTitle(getResources().getString(R.string.account_password_dialog_title))
                           .setView(dialogView)
                           .setPositiveButton(getResources().getString(R.string.dialog_button_confirm),null)
                           .setNegativeButton(getResources().getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {

                               }
                           })
                           .setNeutralButton(getResources().getString(R.string.dialog_button_show_password), null)
                           .show();
                   dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           try{
                               Boolean b=(Boolean) editText.getTag();
                               editText.setTransformationMethod(b?SingleLineTransformationMethod.getInstance():PasswordTransformationMethod.getInstance());
                               editText.setTag(!b);
                           }catch (Exception e){}
                       }
                   });
                   dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           item.password=editText.getText().toString().trim();
                           dialog.cancel();
                           tv_password.setText(getPasswordDisplayValue(item.password));
                       }
                   });
               }
           });

           findViewById(R.id.account_path).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(Build.VERSION.SDK_INT>=23&&PermissionChecker.checkSelfPermission(AccountActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PermissionChecker.PERMISSION_GRANTED){
                       Snackbar snackbar=Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.permission_write_external),Snackbar.LENGTH_SHORT);
                       snackbar.setAction(getResources().getString(R.string.snackbar_action_goto), new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               Intent appdetail = new Intent();
                               appdetail.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                               appdetail.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                               startActivity(appdetail);
                           }
                       });
                       snackbar.show();
                       requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                       return;
                   }
                   /*DialogOfFolderSelector dialog=new DialogOfFolderSelector(AccountActivity.this,item.path);
                   dialog.show();
                   dialog.setOnFolderSelectorDialogConfirmedListener(new DialogOfFolderSelector.OnFolderSelectorDialogConfirmed() {
                       @Override
                       public void onFolderSelectorDialogConfirmed(String path) {
                           item.path=path;
                           ((TextView)findViewById(R.id.account_path_value)).setText(path);
                       }
                   });*/
                   Intent intent=new Intent(AccountActivity.this,FolderSelectorActivity.class);
                   intent.putExtra(FolderSelectorActivity.getEXTRA_CURRENT_PATH(),item.path);
                   startActivityForResult(intent,0);
               }
           });

           findViewById(R.id.account_writable).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   CheckBox cb=findViewById(R.id.account_writable_cb);
                   cb.toggle();
                   item.writable=cb.isChecked();
               }
           });
        }catch (Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void initializeAccountItem();

    private String getPasswordDisplayValue(String password){
        if(password==null||password.trim().length()==0) return getResources().getString(R.string.account_password_att);
        StringBuilder builder=new StringBuilder("");
        for(int i=0;i<password.length();i++){
            builder.append("*");
            if(i>16) break;
        }
        return builder.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0&&resultCode==RESULT_OK&&data!=null){
            final String path=data.getStringExtra(FolderSelectorActivity.getEXTRA_SELECTED_PATH());
            item.path=path;
            ((TextView)findViewById(R.id.account_path_value)).setText(path);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkChangesAndExit();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                checkChangesAndExit();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public long save2DB(@Nullable Long id_update){
        if(FtpService.isFTPServiceRunning()){
            Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.attention_ftp_is_running),Snackbar.LENGTH_SHORT).show();
            return -1;
        }
        if(this.item.account.equals("")){
            Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.account_null_att),Snackbar.LENGTH_SHORT).show();
            return -1;
        }
        if(this.item.account.equals(Constants.FTPConsts.NAME_ANONYMOUS)){
            Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.account_anonymous_name_set_att),Snackbar.LENGTH_SHORT).show();
            return -1;
        }
        for(AccountItem check: FtpService.getUserAccountList(this)){
            if(check.account.equals(this.item.account)&&id_update==null){
                Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.account_duplicate),Snackbar.LENGTH_SHORT).show();
                return -1;
            }
        }
        try{
            return MySQLiteOpenHelper.saveOrUpdateAccountItem2DB(this,item,id_update);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }

        return -1;
    }

    void checkChangesAndExit(){
        if(!FtpService.isFTPServiceRunning()&&!item.toString().equals(checkString)){
            long time=System.currentTimeMillis();
            if(time-first_clicked_back>1000){
                first_clicked_back=time;
                Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.attention_changes_confirm),Snackbar.LENGTH_SHORT).show();
                return;
            }
            setResult(RESULT_CANCELED);
            finish();
        }
        setResult(RESULT_CANCELED);
        finish();
    }
}
