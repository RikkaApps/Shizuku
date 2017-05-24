package moe.shizuku.privileged.api;

import android.database.AbstractCursor;
import android.database.CursorWindow;

import java.util.UUID;

/**
 * Created by Rikka on 2017/5/21.
 */

public class TokenCursor extends AbstractCursor {

    private UUID mToken;

    public TokenCursor(UUID token) {
        mToken = token;
    }

    @Override
    public void fillWindow(int position, CursorWindow window) {
        window.clear();
        window.setStartPosition(position);
        window.setNumColumns(2);
        window.allocRow();
        window.putLong(getLong(0), position, 0);
        window.putLong(getLong(1), position, 1);
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String getString(int column) {
        return null;
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        return 0;
    }

    @Override
    public long getLong(int column) {
        return column == 0 ? mToken.getMostSignificantBits() : mToken.getLeastSignificantBits();
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }
}
