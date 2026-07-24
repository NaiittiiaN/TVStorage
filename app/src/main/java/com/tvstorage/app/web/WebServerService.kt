package com.tvstorage.app.web

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tvstorage.app.data.repository.TelevisionRepository
import com.tvstorage.app.data.entity.TelevisionEntity
import com.tvstorage.app.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.html.*
import javax.inject.Inject
import io.ktor.http.*
import io.ktor.server.request.*

@AndroidEntryPoint
class WebServerService : Service() {

    @Inject
    lateinit var repository: TelevisionRepository

    private var server: ApplicationEngine? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(2002, createNotification())
        
        val localIp = NetworkUtils.getLocalIpAddress() ?: "0.0.0.0"

        server = embeddedServer(CIO, port = 6666, host = localIp) {
            install(ContentNegotiation) { json() }
            install(CORS) {
                anyHost()
                allowHeader(HttpHeaders.ContentType)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Delete)
            }
            routing {
                get("/") {
                    call.respondHtml {
                        head {
                            title("TV Storage Remote")
                            meta(name = "viewport", content = "width=device-width, initial-scale=1")
                            style {
                                unsafe {
                                    +"""
                                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #e0e0e0; margin: 0; padding: 20px; }
                                    .container { max-width: 1000px; margin: 0 auto; }
                                    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: white; padding: 20px; border-radius: 15px; }
                                    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
                                    .card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); border: 2px solid #eee; position: relative; }
                                    .order-num { font-weight: bold; color: #6750A4; font-size: 1.2em; }
                                    .timer { font-size: 2em; font-weight: 900; margin: 10px 0; text-align: center; color: #21005D; }
                                    .brand { color: #555; text-align: center; }
                                    .cost { font-weight: bold; text-align: center; margin-bottom: 15px; }
                                    .actions { display: flex; gap: 8px; flex-wrap: wrap; }
                                    button { padding: 10px; border: none; border-radius: 8px; cursor: pointer; flex: 1; font-weight: bold; }
                                    .btn-main { background: #6750A4; color: white; border-radius: 10px; height: 45px; }
                                    .btn-pause-all { background: #FF9800; color: white; height: 45px; padding: 0 15px; border-radius: 10px; margin-right: 10px; }
                                    .btn-pause { background: #f5f5f5; color: #333; }
                                    .btn-edit { background: #EADDFF; color: #21005D; }
                                    .btn-del { background: #ffebee; color: #c62828; }
                                    .status-4-6 { background: #E8F5E9; border-color: #C8E6C9; }
                                    .status-7-9 { background: #FFF3E0; border-color: #FFE0B2; }
                                    .status-10 { background: #FFEBEE; border-color: #FFCDD2; }
                                    .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 1000; overflow-y: auto; }
                                    .modal-content { background: white; margin: 5% auto; padding: 30px; border-radius: 20px; width: 90%; max-width: 400px; }
                                    input, textarea { width: 100%; padding: 12px; margin-top: 5px; border: 1px solid #ddd; border-radius: 8px; box-sizing: border-box; }
                                    .adv-section { margin-top: 15px; background: #fafafa; padding: 15px; border-radius: 10px; border: 1px solid #eee; }
                                    """.trimIndent()
                                }
                            }
                        }
                        body {
                            div("container") {
                                div("header") {
                                    h1 { +"TV Storage Remote" }
                                    div {
                                        button(classes = "btn-pause-all") { id = "pause-all-btn"; onClick = "pauseAll()"; +"⏸ Пауза для всех" }
                                        button(classes = "btn-main") { onClick = "openModal()"; +"➕ Добавить ТВ" }
                                    }
                                }
                                div("grid") { id = "tv-grid"; +"Загрузка..." }
                            }

                            div("modal") {
                                id = "tvModal"
                                div("modal-content") {
                                    h2 { id = "modal-title"; +"Редактирование" }
                                    input(type = InputType.hidden) { id = "m-id" }
                                    label { +"S/N *" }; input(type = InputType.text) { id = "m-ord" }
                                    label { +"Бренд" }; input(type = InputType.text) { id = "m-brd" }
                                    label { +"Модель" }; input(type = InputType.text) { id = "m-mdl" }
                                    label { +"ФИО клиента" }; input(type = InputType.text) { id = "m-cln" }
                                    label { +"Стоимость/день" }; input(type = InputType.number) { id = "m-cst" }
                                    label { +"Примечания" }; textArea { id = "m-nts"; rows = "3" }
                                    
                                    div("adv-section") {
                                        label { style="font-weight:bold"; +"Дата и время приемки" }
                                        input(type = InputType.dateTimeLocal) { id = "m-date" }
                                    }

                                    div("actions") {
                                        style = "margin-top: 20px"
                                        button { style="background:#eee"; onClick = "closeModal()"; +"Отмена" }
                                        button(classes = "btn-main") { onClick = "saveTv()"; +"Сохранить" }
                                    }
                                }
                            }

                            script {
                                unsafe {
                                    +"""
                                    let isEdit = false;
                                    let globalPaused = false;

                                    function openModal(tv = null) {
                                        isEdit = !!tv;
                                        document.getElementById('modal-title').innerText = isEdit ? 'Редактировать ТВ' : 'Новый заказ';
                                        document.getElementById('m-id').value = tv ? tv.id : '';
                                        document.getElementById('m-ord').value = tv ? tv.orderNumber : '';
                                        document.getElementById('m-brd').value = tv ? tv.brand : '';
                                        document.getElementById('m-mdl').value = tv ? tv.model : '';
                                        document.getElementById('m-cln').value = tv ? tv.clientName : '';
                                        document.getElementById('m-cst').value = tv ? tv.dailyCost : '100';
                                        document.getElementById('m-nts').value = tv ? tv.notes : '';
                                        
                                        const date = tv ? new Date(tv.receivedDate) : new Date();
                                        const localIso = new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
                                        document.getElementById('m-date').value = localIso;

                                        document.getElementById('tvModal').style.display='block';
                                    }
                                    function closeModal() { document.getElementById('tvModal').style.display='none'; }

                                    async function saveTv() {
                                        const id = document.getElementById('m-id').value;
                                        const dateVal = document.getElementById('m-date').value;
                                        const tv = {
                                            id: id ? parseInt(id) : 0,
                                            orderNumber: document.getElementById('m-ord').value,
                                            brand: document.getElementById('m-brd').value,
                                            model: document.getElementById('m-mdl').value,
                                            clientName: document.getElementById('m-cln').value,
                                            dailyCost: parseFloat(document.getElementById('m-cst').value) || 100,
                                            notes: document.getElementById('m-nts').value,
                                            receivedDate: new Date(dateVal).getTime()
                                        };
                                        if(!tv.orderNumber) return alert('S/N!');
                                        const method = isEdit ? 'PUT' : 'POST';
                                        const url = isEdit ? '/api/televisions/' + id : '/api/televisions';
                                        const r = await fetch(url, {
                                            method: method,
                                            headers: {'Content-Type': 'application/json'},
                                            body: JSON.stringify(tv)
                                        });
                                        if(r.ok) { closeModal(); load(); } else { alert(await r.text()); }
                                    }

                                    async function pauseAll() {
                                        globalPaused = !globalPaused;
                                        await fetch('/api/televisions/pause-all?isPaused=' + globalPaused, { method: 'PUT' });
                                        load();
                                    }

                                    async function load() {
                                        const res = await fetch('/api/televisions');
                                        const tvs = await res.json();
                                        const grid = document.getElementById('tv-grid');
                                        grid.innerHTML = tvs.length === 0 ? '<div style="text-align:center;width:100%;margin-top:50px">Пусто</div>' : '';
                                        
                                        if (tvs.length > 0) {
                                            globalPaused = tvs.every(t => t.isPaused);
                                            document.getElementById('pause-all-btn').innerText = globalPaused ? '▶ Запустить все' : '⏸ Пауза для всех';
                                        }

                                        tvs.forEach(tv => {
                                            const days = Math.floor((Date.now() - tv.receivedDate)/(1000*60*60*24));
                                            let cls = days >= 10 ? 'status-10' : days >= 7 ? 'status-7-9' : days >= 4 ? 'status-4-6' : '';
                                            const card = document.createElement('div');
                                            card.className = 'card ' + cls;
                                            card.innerHTML = `
                                                <div style='display:flex;justify-content:space-between'>
                                                    <div class='order-num'>№ ${'$'}{tv.orderNumber}</div>
                                                    <div style='font-size:0.8em;color:#888'>${'$'}{tv.isPaused ? '⏸ ПАУЗА' : ''}</div>
                                                </div>
                                                <div class='brand'>${'$'}{tv.brand} ${'$'}{tv.model}</div>
                                                <div class='timer'>${'$'}{days}д</div>
                                                <div class='cost'>${'$'}{Math.floor(tv.dailyCost * days)} ₽</div>
                                                <div class='actions'>
                                                    <button class='btn-pause' onclick='window.pause(${'$'}{tv.id})'>${'$'}{tv.isPaused ? 'Пуск' : 'Пауза'}</button>
                                                    <button class='btn-edit' onclick='window.editTv(${'$'}{JSON.stringify(tv).replace(/'/g, "&apos;")})'>Правка</button>
                                                    <button class='btn-del' onclick='window.del(${'$'}{tv.id})'>Удалить</button>
                                                </div>
                                            `;
                                            grid.appendChild(card);
                                        });
                                    }
                                    window.pause = async (id) => { await fetch('/api/televisions/'+id+'/pause', {method:'PUT'}); load(); };
                                    window.editTv = (tv) => openModal(tv);
                                    window.del = async (id) => { if(confirm('Удалить?')) { await fetch('/api/televisions/'+id, {method:'DELETE'}); load(); } };
                                    load();
                                    setInterval(load, 30000);
                                    """.trimIndent()
                                }
                            }
                        }
                    }
                }
                route("/api/televisions") {
                    get { call.respond(repository.getAllActive().first()) }
                    post {
                        val tv = call.receive<TelevisionEntity>()
                        if (repository.getByOrderNumber(tv.orderNumber) != null) {
                            call.respond(HttpStatusCode.Conflict, "Дубликат S/N")
                        } else {
                            repository.insert(tv)
                            call.respond(HttpStatusCode.Created)
                        }
                    }
                    put("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                        val updated = call.receive<TelevisionEntity>()
                        val existing = repository.getById(id) ?: return@put call.respond(HttpStatusCode.NotFound)
                        repository.update(updated.copy(id = id, receivedDate = updated.receivedDate, createdAt = existing.createdAt))
                        call.respond(HttpStatusCode.OK)
                    }
                    put("/{id}/pause") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                        val tv = repository.getById(id) ?: return@put call.respond(HttpStatusCode.NotFound)
                        repository.update(tv.copy(isPaused = !tv.isPaused))
                        call.respond(HttpStatusCode.OK)
                    }
                    put("/pause-all") {
                        val isPaused = call.request.queryParameters["isPaused"]?.toBoolean() ?: true
                        repository.setAllPaused(isPaused)
                        call.respond(HttpStatusCode.OK)
                    }
                    delete("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        val tv = repository.getById(id) ?: return@delete call.respond(HttpStatusCode.NotFound)
                        repository.delete(tv)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }.start(wait = false)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("web_server", "Web Server", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification() = NotificationCompat.Builder(this, "web_server")
        .setContentTitle("Web Server Active")
        .setContentText("Удаленный доступ доступен по порту 6666")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        server?.stop(1000, 5000)
        serviceScope.cancel()
        super.onDestroy()
    }
}
