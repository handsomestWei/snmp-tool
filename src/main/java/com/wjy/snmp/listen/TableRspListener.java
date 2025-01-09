package com.wjy.snmp.listen;

import com.wjy.snmp.SnmpHelper;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TableUtils自定义异步响应处理器
 *
 * @author weijiayu
 * @date 2025/1/8 15:50
 * @see org.snmp4j.util.TableUtils.InternalTableListener
 * @see com.wjy.snmp.AbsSnmpBase.dispatchPoolSize 需要配置合理的异步处理线程池
 */
@Slf4j
public class TableRspListener implements TableListener {

    private List<TableEvent> rows = new LinkedList<TableEvent>();
    private volatile boolean finished = false;
    private Consumer<HashMap<String, String>> asyncRspHdl;

    public TableRspListener(Consumer<HashMap<String, String>> asyncRspHdl) {
        this.asyncRspHdl = asyncRspHdl;
    }

    @Override
    public boolean next(TableEvent event) {
        rows.add(event);
        return true;
    }

    @Override
    public synchronized void finished(TableEvent event) {
        if ((event.getStatus() != TableEvent.STATUS_OK) ||
                (event.getIndex() != null)) {
            rows.add(event);
        }
        finished = true;
        notify();
        callAsyncRspHdl();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public List<TableEvent> getRows() {
        return rows;
    }

    private void callAsyncRspHdl() {
        log.debug("snmp get table async rsp {}", this.rows);
        if (this.asyncRspHdl != null) {
            HashMap<String, String> vbMap = SnmpHelper.convertTbEventList(this.rows);
            asyncRspHdl.accept(vbMap);
        }
    }
}
