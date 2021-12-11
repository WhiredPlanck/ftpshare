package com.github.ghmxr.ftpshare.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.github.ghmxr.ftpshare.R;
import com.github.ghmxr.ftpshare.data.AccountItem;
import com.github.ghmxr.ftpshare.services.FtpService;

public class AddAccountActivity extends AccountActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            getSupportActionBar().setTitle(getResources().getString(R.string.activity_title_add));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initializeAccountItem() {
        item=new AccountItem();
        item.account="user"+ (FtpService.getUserAccountList(this).size()+1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_add,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_account_add_save){
            if(save2DB(null)>=0) {
                setResult(RESULT_OK);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
