package com.fleetpin.graphql.database.manager;

import com.fleetpin.graphql.database.manager.util.BackupItem;
import com.fleetpin.graphql.database.manager.util.HistoryBackupItem;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class VirtualDatabase {

	public static ExecutorService VIRTUAL_THREAD_POOL = Executors.newVirtualThreadPerTaskExecutor();

	private final Database database;

	private final AtomicInteger submitted;

	public VirtualDatabase(Database database) {
		this.database = database;
		this.submitted = new AtomicInteger();
	}

	public <T extends Table> List<T> delete(String organisationId, Class<T> type) {
		var future = database.delete(organisationId, type);
		return handleFuture(future);
	}

	public <T extends Table> T delete(T entity, boolean deleteLinks) {
		var future = database.delete(entity, deleteLinks);
		return handleFuture(future);
	}

	public <T extends Table> List<T> getLinks(final Table entry, Class<T> target) {
		return handleFuture(database.getLinks(entry, target));
	}

	public Boolean destroyOrganisation(final String organisationId) {
		return handleFuture(database.destroyOrganisation(organisationId));
	}

	public <T extends Table> T get(Class<T> type, String id) {
		var future = database.get(type, id);
		return handleFuture(future);
	}

	public <T extends Table> List<T> get(Class<T> type, List<String> ids) {
		var future = database.get(type, ids);
		return handleFuture(future);
	}

	public <T extends Table> T getLink(final Table entry, Class<T> target) {
		var future = database.getLink(entry, target);
		return handleFuture(future);
	}

	public Set<String> getLinkIds(Table entity, Class<? extends Table> type) {
		return database.getLinkIds(entity, type);
	}

	public <T extends Table> Optional<T> getLinkOptional(final Table entry, Class<T> target) {
		var future = database.getLinkOptional(entry, target);
		return handleFuture(future);
	}

	public <T extends Table> Optional<T> getOptional(Class<T> type, String id) {
		var future = database.getOptional(type, id);
		return handleFuture(future);
	}

	public String getSourceOrganisationId(Table entity) {
		return database.getSourceOrganisationId(entity);
	}

	public <T extends Table> T link(T entity, Class<? extends Table> class1, String targetId) {
		return handleFuture(database.link(entity, class1, targetId));
	}

	public <T extends Table> T links(T entity, Class<? extends Table> class1, List<String> targetIds) {
		return handleFuture(database.links(entity, class1, targetIds));
	}

	public String newId() {
		return database.newId();
	}

	public <T extends Table> T put(T entity) {
		return handleFuture(database.put(entity));
	}

	public <T extends Table> T put(T entity, boolean check) {
		return handleFuture(database.put(entity, check));
	}

	public <T extends Table> T putGlobal(T entity) {
		return handleFuture(database.putGlobal(entity));
	}

	public <T extends Table> List<T> query(Class<T> type) {
		var future = database.query(type);
		return handleFuture(future);
	}

	public <T extends Table> List<T> query(Query<T> query) {
		var future = database.query(query);
		return handleFuture(future);
	}

	public <T extends Table> List<T> query(Class<T> type, Function<QueryBuilder<T>, QueryBuilder<T>> func) {
		var future = database.query(type, func);
		return handleFuture(future);
	}

	public <T extends Table> List<T> queryGlobal(Class<T> type, String id) {
		var future = database.queryGlobal(type, id);
		return handleFuture(future);
	}

	public <T extends Table> T queryGlobalUnique(Class<T> type, String id) {
		var future = database.queryGlobalUnique(type, id);
		return handleFuture(future);
	}

	public <T extends Table> List<T> queryHistory(QueryHistory<T> query) {
		var future = database.queryHistory(query);
		return handleFuture(future);
	}

	public <T extends Table> List<T> querySecondary(Class<T> type, String id) {
		var future = database.querySecondary(type, id);
		return handleFuture(future);
	}

	public <T extends Table> T querySecondaryUnique(Class<T> type, String id) {
		var future = database.querySecondaryUnique(type, id);
		return handleFuture(future);
	}

	public <T extends Table> Void restoreBackup(List<BackupItem> entities) {
		var future = database.restoreBackup(entities);
		return handleFuture(future);
	}

	public <T extends Table> Void restoreHistoryBackup(List<HistoryBackupItem> entities) {
		var future = database.restoreHistoryBackup(entities);
		return handleFuture(future);
	}

	public void setOrganisationId(String organisationId) {
		database.setOrganisationId(organisationId);
	}

	public List<BackupItem> takeBackup(String organisationId) {
		var future = database.takeBackup(organisationId);
		return handleFuture(future);
	}

	public List<HistoryBackupItem> takeHistoryBackup(String organisationId) {
		var future = database.takeHistoryBackup(organisationId);
		return handleFuture(future);
	}

	public <T extends Table> T takeHistoryBackup(final T entity, final Class<? extends Table> type, final String targetId) {
		var future = database.unlink(entity, type, targetId);
		return handleFuture(future);
	}

	public <T> T handleFuture(CompletableFuture<T> future) {
		if (future.isDone()) {
			return future.join();
		}

		if (submitted.get() == 0) {
			if (submitted.compareAndSet(0, 1)) {
				run();
			} else {
				submitted.incrementAndGet();
			}
		} else {
			submitted.incrementAndGet();
		}
		return future.join();
	}

	private void run() {
		VIRTUAL_THREAD_POOL.submit(() -> {
			while (true) {
				var start = submitted.get();
				database.start();
				if (submitted.compareAndSet(start, 0)) {
					break;
				}
			}
		});
	}
}
