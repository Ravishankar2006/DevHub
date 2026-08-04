package com.devhub.notes;

import com.devhub.common.ApiException;
import com.devhub.notes.dto.*;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteFolderRepository noteFolderRepository;

    @Transactional
    public NoteDto createNote(User currentUser, NoteRequest request) {
        NoteFolder folder = resolveFolder(currentUser, request.getFolderId());

        Note note = Note.builder()
                .user(currentUser)
                .folder(folder)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(NoteMapper.joinTags(request.getTags()))
                .build();

        note = noteRepository.save(note);
        return NoteMapper.toDto(note);
    }

    @Transactional(readOnly = true)
    public List<NoteDto> listNotes(User currentUser, UUID folderId, String q) {
        List<Note> notes;
        if (StringUtils.hasText(q)) {
            notes = noteRepository.search(currentUser.getId(), q);
            if (folderId != null) {
                notes = notes.stream()
                        .filter(n -> n.getFolder() != null && n.getFolder().getId().equals(folderId))
                        .collect(Collectors.toList());
            }
        } else if (folderId != null) {
            notes = noteRepository.findByUserIdAndFolderIdOrderByUpdatedAtDesc(currentUser.getId(), folderId);
        } else {
            notes = noteRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId());
        }
        return notes.stream().map(NoteMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NoteDto getNote(User currentUser, UUID noteId) {
        return NoteMapper.toDto(getOwnedNote(currentUser, noteId));
    }

    @Transactional
    public NoteDto updateNote(User currentUser, UUID noteId, NoteRequest request) {
        Note note = getOwnedNote(currentUser, noteId);
        NoteFolder folder = resolveFolder(currentUser, request.getFolderId());

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTags(NoteMapper.joinTags(request.getTags()));
        note.setFolder(folder);

        note = noteRepository.save(note);
        return NoteMapper.toDto(note);
    }

    @Transactional
    public void deleteNote(User currentUser, UUID noteId) {
        Note note = getOwnedNote(currentUser, noteId);
        noteRepository.delete(note);
    }

    @Transactional
    public NoteFolderDto createFolder(User currentUser, NoteFolderRequest request) {
        NoteFolder folder = NoteFolder.builder()
                .user(currentUser)
                .name(request.getName())
                .build();

        folder = noteFolderRepository.save(folder);
        return NoteFolderMapper.toDto(folder, 0);
    }

    @Transactional(readOnly = true)
    public List<NoteFolderDto> listFolders(User currentUser) {
        return noteFolderRepository.findByUserIdOrderByNameAsc(currentUser.getId()).stream()
                .map(folder -> NoteFolderMapper.toDto(folder, noteRepository.countByFolderId(folder.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public NoteFolderDto updateFolder(User currentUser, UUID folderId, NoteFolderRequest request) {
        NoteFolder folder = getOwnedFolder(currentUser, folderId);
        folder.setName(request.getName());
        folder = noteFolderRepository.save(folder);
        return NoteFolderMapper.toDto(folder, noteRepository.countByFolderId(folder.getId()));
    }

    @Transactional
    public void deleteFolder(User currentUser, UUID folderId) {
        NoteFolder folder = getOwnedFolder(currentUser, folderId);

        // Unlink notes rather than deleting them - a folder going away shouldn't
        // take its notes with it (SET NULL semantics, applied at the service layer
        // for the same H2-vs-Postgres schema-generation reason as project deletion).
        List<Note> linkedNotes = noteRepository.findByFolderId(folderId);
        linkedNotes.forEach(note -> note.setFolder(null));
        noteRepository.saveAll(linkedNotes);

        noteFolderRepository.delete(folder);
    }

    private Note getOwnedNote(User currentUser, UUID noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ApiException("Note not found", HttpStatus.NOT_FOUND));

        if (!note.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Note not found", HttpStatus.NOT_FOUND);
        }
        return note;
    }

    private NoteFolder getOwnedFolder(User currentUser, UUID folderId) {
        NoteFolder folder = noteFolderRepository.findById(folderId)
                .orElseThrow(() -> new ApiException("Folder not found", HttpStatus.NOT_FOUND));

        if (!folder.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Folder not found", HttpStatus.NOT_FOUND);
        }
        return folder;
    }

    private NoteFolder resolveFolder(User currentUser, UUID folderId) {
        if (folderId == null) return null;
        return getOwnedFolder(currentUser, folderId);
    }
}
